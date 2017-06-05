package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatProvider;
import com.kayhut.fuse.epb.plan.statistics.util.StatConfigTestUtil;
import com.kayhut.fuse.epb.plan.statistics.util.StatTestUtil;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 28-May-17.
 */
public class ElasticStatProviderTest {

    //region Parameters
    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static StatConfig statConfig;

    private static Iterable<Map<String, Object>> dragonsList;

    private static final String MAPPING_DATA_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.data.dragon.mapping.json").toString();
    private static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();

    private static final int NUM_OF_DRAGONS_IN_INDEX = 1000;

    private static final String DATA_INDEX_NAME = "index1";
    private static final String DATA_TYPE_NAME = "Dragon";
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_NAME = "name";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "_type";

    private static final int DRAGON_MIN_AGE = 1;
    private static final int DRAGON_MAX_AGE = 10;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");


    // The name of the Elastic cluster
    private static final String DATA_CLUSTER_NAME = "fuse.test_elastic";

    // A list of hostnames for of the nodes in the cluster
    private static final String[] DATA_HOSTS = new String[]{"localhost"};

    // The transport port for the cluster
    private static final int DATA_TRANSPORT_PORT = 9300;

    // The name of the statistics cluster
    private static final String STAT_CLUSTER_NAME = "fuse.test_elastic";

    //A list of hostnames for of the nodes in the statistics cluster
    private static final String[] STAT_HOSTS = new String[]{"localhost"};

    // The transport port for the statistics cluster
    private static final int STAT_TRANSPORT_PORT = 9300;

    static Logger logger = org.slf4j.LoggerFactory.getLogger(ElasticStatProviderTest.class);
    //endregion

    //todo Add more tests

    @Test
    public void getGenderFieldStatisticsTest() throws Exception {
        ElasticStatProvider elasticStatProvider = new ElasticStatProvider(statConfig);

        List<Statistics.BucketInfo> genderStatistics = elasticStatProvider.getFieldStatistics(statClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
                Arrays.asList(DATA_INDEX_NAME),
                Arrays.asList(DATA_TYPE_NAME),
                Arrays.asList(DATA_FIELD_NAME_GENDER));


        assertEquals(DRAGON_GENDERS.size(), genderStatistics.size());
        assertNotNull(genderStatistics.get(0));
        //Cardinality of specific gender should be 1
        assertEquals(new Long(1), genderStatistics.get(0).getCardinality());
        genderStatistics.forEach(bucketInfo -> {
            //Since this is a term: Lower bound value == Upper bound value
            assertEquals(bucketInfo.getLowerBound(), bucketInfo.getHigherBound());
        });
    }


    @Test
    public void getAgeFieldStatisticsTest() throws Exception {
        ElasticStatProvider elasticStatProvider = new ElasticStatProvider(statConfig);
        List<Statistics.BucketInfo> ageStatistics = elasticStatProvider.getFieldStatistics(statClient,
                statConfig.getStatIndexName(),
                statConfig.getStatNumericTypeName(),
                Arrays.asList(DATA_INDEX_NAME),
                Arrays.asList(DATA_TYPE_NAME),
                Arrays.asList(DATA_FIELD_NAME_AGE));

        assertTrue(ageStatistics.size() > 0);

        ageStatistics.forEach(bucketInfo -> {
            //Since this is a term: Lower bound value == Upper bound value
            assertTrue((Double) bucketInfo.getLowerBound() <= (Double) bucketInfo.getHigherBound());
        });
    }


    @BeforeClass
    public static void setUp() throws Exception {

        statConfig = StatConfigTestUtil.getStatConfig(buildStatContainer());

        MappingFileElasticConfigurer configurerIndex1 = new MappingFileElasticConfigurer(DATA_INDEX_NAME, MAPPING_DATA_FILE_PATH);
        MappingFileElasticConfigurer configurerStat = new MappingFileElasticConfigurer(statConfig.getStatIndexName(), MAPPING_STAT_FILE_PATH);

        elasticEmbeddedNode = new ElasticEmbeddedNode(configurerIndex1, configurerStat);


        dataClient = ClientProvider.getTransportClient(DATA_CLUSTER_NAME, DATA_TRANSPORT_PORT, DATA_HOSTS);
        statClient = ClientProvider.getTransportClient(STAT_CLUSTER_NAME, STAT_TRANSPORT_PORT, STAT_HOSTS);

        dragonsList = StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX,
                DRAGON_MIN_AGE,
                DRAGON_MAX_AGE,
                DRAGON_NAME_PREFIX_LENGTH,
                DRAGON_COLORS,
                DRAGON_GENDERS,
                DRAGON_ADDRESS_LENGTH);

        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                "id",
                () -> dragonsList
        ).populate();

        Thread.sleep(2000);
        StatCalculator.loadDefaultStatParameters(
                statConfig.getStatIndexName(),
                statConfig.getStatNumericTypeName(),
                statConfig.getStatStringTypeName(),
                statConfig.getStatTermTypeName(),
                statConfig.getStatGlobalTypeName());
        StatCalculator.buildStatisticsBasedOnConfiguration(dataClient, statClient, buildStatContainer());
        Thread.sleep(3000);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (statClient != null) {
            statClient.close();
            statClient = null;
        }

        if (dataClient != null) {
            dataClient.close();
            dataClient = null;
        }

        elasticEmbeddedNode.close();
        Thread.sleep(4000);

    }

    //Per test
    private static StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.get()
                .withMin(DRAGON_MIN_AGE).withMax(DRAGON_MAX_AGE).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.get()
                .withPrefixSize(3)
                .withInterval(10)
                .withNumOfChars(26)
                .withFirstCharCode("97")
                .build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                ))
                .withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.get()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.get()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.get()
                .withDataType(DataType.string)
                .withTerms(DRAGON_GENDERS)
                .build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.get()
                .withDataType(DataType.string)
                .withTerm(DATA_TYPE_NAME) // "Dragon"
                .build();


        Type typeDragon = Type.Builder.instance()
                .withType(DATA_TYPE_NAME)
                .withField(new Field(DATA_FIELD_NAME_AGE, histogramDragonAge))
                .withField(new Field(DATA_FIELD_NAME_NAME, histogramDragonName))
                .withField(new Field(DATA_FIELD_NAME_ADDRESS, histogramDragonAddress))
                .withField(new Field(DATA_FIELD_NAME_COLOR, histogramDragonColor))
                .withField(new Field(DATA_FIELD_NAME_GENDER, histogramTerm))
                .withField(new Field(DATA_FIELD_NAME_TYPE, histogramDocType))
                .build();

        Mapping mapping = Mapping.Builder.get()
                .withIndex(DATA_INDEX_NAME)
                .withType(DATA_TYPE_NAME)
                .build();

        return StatContainer.Builder.get()
                .withMapping(mapping)
                .withType(typeDragon)
                .build();
    }
}