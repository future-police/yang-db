package com.kayhut.fuse.services.engine2.data;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.fuse.services.engine2.JoinE2EEpbMockTestSuite;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.services.engine2.mocks.EpbMockModule;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.test.framework.index.MappingElasticConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.index.Mappings;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static java.util.Collections.singletonList;

public class JoinE2EEpbMockTests {
    @BeforeClass
    public static void setup() throws Exception {
        setup(JoinE2EEpbMockTestSuite.elasticEmbeddedNode.getClient(), true);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanup(JoinE2EEpbMockTestSuite.elasticEmbeddedNode.getClient());
    }


    public static void setup(TransportClient client, boolean calcStats) throws Exception {
        fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));

        String idField = "id";

        new MappingElasticConfigurer(OntologyTestUtils.PERSON.name.toLowerCase(), new Mappings().addMapping("pge", getPersonMapping()))
                .configure(client);
        new MappingElasticConfigurer(OntologyTestUtils.DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingElasticConfigurer(Arrays.asList(
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"),
                new Mappings().addMapping("pge", getFireMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        new ElasticDataPopulator(
                client,
                OntologyTestUtils.PERSON.name.toLowerCase(),
                "pge",
                idField,
                () -> createPeople(10)).populate();

        new ElasticDataPopulator(
                client,
                OntologyTestUtils.DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(10, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170511",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170512",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-12 00:00:00").getTime()).apply(600000L),
                        temperatureValueFunction))
                .populate(); // date interval is 10 min

        new ElasticDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170513",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        10,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-13 00:00:00").getTime()).apply(300000L),
                        temperatureValueFunction))
                .populate(); // date interval is 5 min


        client.admin().indices().refresh(new RefreshRequest(
                OntologyTestUtils.PERSON.name.toLowerCase(),
                OntologyTestUtils.DRAGON.name.toLowerCase(),
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"
        )).actionGet();

        if(calcStats){
            new MappingFileElasticConfigurer("stat", "src/test/resources/stat_mappings.json").configure(client);
            Configuration statConfig = new StatConfiguration("statistics.test.properties").getInstance();
            StatCalculator.run(client, client, statConfig);
            client.admin().indices().refresh(new RefreshRequest("stat")).actionGet();
        }
    }

    public static void cleanup(TransportClient client) throws Exception {
        cleanup(client, false);
    }

    public static void cleanup(TransportClient client, boolean statsUsed) throws Exception {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        OntologyTestUtils.PERSON.name.toLowerCase(),
                        OntologyTestUtils.DRAGON.name.toLowerCase(),
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513"))
                .actionGet();

        if(statsUsed){
            client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    private void testAndAssertQuery(Query query, QueryResult expectedQueryResult) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        QueryResultAssert.assertEquals(expectedQueryResult, actualQueryResult, shouldIgnoreRelId());
    }



    private static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "Person_" + i);
            person.put("type", "Person");
            person.put(NAME.name, "person" + i);
            people.add(person);
        }
        return people;
    }

    private static Mappings.Mapping getPersonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Iterable<Map<String, Object>> createDragons(
            int numDragons,
            Function<Integer, Long> birthDateValueFunction) {

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", OntologyTestUtils.DRAGON.name);
            dragon.put(NAME.name, OntologyTestUtils.DRAGON.name + i);
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Mappings.Mapping getDragonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(BIRTH_DATE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
    }


    private static Iterable<Map<String, Object>> createDragonFireDragonEdges(
            int numDragons,
            Function<Integer, Long> timestampValueFunction,
            Function<Integer, Integer> temperatureValueFunction
    ) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        int counter = 0;
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0 ; j < i ; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", FIRE.getName() + counter);
                fireEdge.put("type", FIRE.getName());
                fireEdge.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdge.put("direction", Direction.OUT);
                fireEdge.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", FIRE.getName() + counter + 1);
                fireEdgeDual.put("type", FIRE.getName());
                fireEdgeDual.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdgeDual.put("direction", Direction.IN);
                fireEdgeDual.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", DRAGON.name);
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", DRAGON.name);
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", DRAGON.name);
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", DRAGON.name);

                fireEdge.put("entityA", entityAI);
                fireEdge.put("entityB", entityBJ);
                fireEdgeDual.put("entityA", entityAJ);
                fireEdgeDual.put("entityB", entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                counter += 2;
            }
        }

        return fireEdges;
    }

    private static Mappings.Mapping getFireMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TIMESTAMP.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"))
                .addProperty("direction", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TEMPERATURE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.integer))
                .addProperty("entityA", new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty("entityB", new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }
    //endregion

    @Test
    public void testDragonFireDragonPathMiddleJoin() throws IOException, InterruptedException {
        Query query = getQuery();

        RedundantRelProp redundantRelProp = new RedundantRelProp("entityB.type");
        redundantRelProp.setpType("type");
        redundantRelProp.setCon(Constraint.of(ConstraintOp.inSet, Stream.of("Dragon").toArray(), "[]"));


        Plan left = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(1))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>((Rel)query.getElements().get(2))),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Collections.singletonList(redundantRelProp)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(3))));


        Rel relation = (Rel) query.getElements().get(4).clone();
        relation.setDir(Rel.Direction.R);
        Plan right = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(5))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>(relation)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Collections.singletonList(redundantRelProp)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(3))));

        Plan injectedPlan = new Plan(new EntityJoinOp(left, right));

        setCurrentPlan(new PlanWithCost<>(injectedPlan, new PlanDetailedCost(new DoubleCost(0),
                Collections.singleton(new PlanWithCost<>(injectedPlan, new JoinCost(1,1,new PlanDetailedCost(new DoubleCost(10),Collections.singleton(new PlanWithCost<>(left, new CountEstimatesCost(1,1)))),
                        new PlanDetailedCost(new DoubleCost(10),Collections.singleton(new PlanWithCost<>(right, new CountEstimatesCost(1,1))))))))));

        runQueryAndValidate(query,dragonFireDragonResults());
    }

    @Test
    public void testDragonFireDragonPathMiddleJoinSwitchBranches() throws IOException, InterruptedException {
        Query query = getQuery();

        RedundantRelProp redundantRelProp = new RedundantRelProp("entityB.type");
        redundantRelProp.setpType("type");
        redundantRelProp.setCon(Constraint.of(ConstraintOp.inSet, Stream.of("Dragon").toArray(), "[]"));


        Plan left = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(1))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>((Rel)query.getElements().get(2))),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Collections.singletonList(redundantRelProp)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(3))));


        Rel relation = (Rel) query.getElements().get(4).clone();
        relation.setDir(Rel.Direction.R);
        Plan right = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(5))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>(relation)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Collections.singletonList(redundantRelProp)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(3))));

        Plan injectedPlan = new Plan(new EntityJoinOp(left, right));

        setCurrentPlan(new PlanWithCost<>(injectedPlan, new PlanDetailedCost(new DoubleCost(0),
                Collections.singleton(new PlanWithCost<>(injectedPlan, new JoinCost(1,1,new PlanDetailedCost(new DoubleCost(5),Collections.singleton(new PlanWithCost<>(left, new CountEstimatesCost(1,1)))),
                        new PlanDetailedCost(new DoubleCost(10),Collections.singleton(new PlanWithCost<>(right, new CountEstimatesCost(1,1))))))))));

        runQueryAndValidate(query,dragonFireDragonResults());
    }

    @Test
    public void testDragonFireDragonPathStartJoin() throws IOException, InterruptedException {
        Query query = getQuery();

        RedundantRelProp redundantRelProp = new RedundantRelProp("entityB.type");
        redundantRelProp.setpType("type");
        redundantRelProp.setCon(Constraint.of(ConstraintOp.inSet, Stream.of("Dragon").toArray(), "[]"));

        RedundantRelProp redundantRelProp2 = new RedundantRelProp("entityB.id");
        redundantRelProp2.setpType("id");
        redundantRelProp2.setCon(Constraint.of(ConstraintOp.eq, "Dragon_4", "[]"));

        Plan left = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(1))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())));


        Rel relation4 = (Rel) query.getElements().get(4).clone();
        relation4.setDir(Rel.Direction.R);
        Rel relation2 = (Rel) query.getElements().get(2).clone();
        relation2.setDir(Rel.Direction.L);
        Plan right = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(5))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>(relation4)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Collections.singletonList(redundantRelProp)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(3))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())),
                new RelationOp(new AsgEBase<>(relation2)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(Arrays.asList(redundantRelProp,redundantRelProp2)))),
                new EntityOp(new AsgEBase<>((EEntityBase)query.getElements().get(1))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup())));

        Plan injectedPlan = new Plan(new EntityJoinOp(left, right));

        setCurrentPlan(new PlanWithCost<>(injectedPlan, new PlanDetailedCost(new DoubleCost(0),
                Collections.singleton(new PlanWithCost<>(injectedPlan, new JoinCost(1,1,new PlanDetailedCost(new DoubleCost(10),Collections.singleton(new PlanWithCost<>(left, new CountEstimatesCost(1,1)))),
                        new PlanDetailedCost(new DoubleCost(10),Collections.singleton(new PlanWithCost<>(right, new CountEstimatesCost(1,1))))))))));

        runQueryAndValidate(query,dragonFireDragonResults());
    }

    private void runQueryAndValidate(Query query, QueryResult expectedQueryResult) throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        QueryResult actualQueryResult = fuseClient.getPageData(pageResourceInfo.getDataUrl());
        QueryResultAssert.assertEquals(expectedQueryResult, actualQueryResult, shouldIgnoreRelId());
    }

    private Query getQuery() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DRAGON.name), "Dragon_4", "D0", singletonList(NAME.type), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), singletonList(NAME.type), 4, 0),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.L, null, 5, 0),
                new EConcrete(5, "C", $ont.eType$(DRAGON.name), "Dragon_9", "D1", singletonList(NAME.type), 0, 0)
        )).build();
    }

    private QueryResult dragonFireDragonResults() {
        QueryResult.Builder builder = QueryResult.Builder.instance();
        Entity entityA = Entity.Builder.instance()
                .withEID("Dragon_4" )
                .withETag(singletonList("A"))
                .withEType($ont.eType$(DRAGON.name))
                .withProperties(singletonList(
                        new com.kayhut.fuse.model.results.Property(NAME.type, "raw", DRAGON.name + 4)))
                .build();

        Entity entityC = Entity.Builder.instance()
                .withEID("Dragon_9" )
                .withETag(singletonList("C"))
                .withEType($ont.eType$(DRAGON.name))
                .withProperties(singletonList(
                        new com.kayhut.fuse.model.results.Property(NAME.type, "raw", DRAGON.name + 9)))
                .build();

        for(int i = 0;i<4;i++){
            Entity entityB = Entity.Builder.instance()
                    .withEID("Dragon_"+i )
                    .withETag(singletonList("B"))
                    .withEType($ont.eType$(DRAGON.name))
                    .withProperties(singletonList(
                            new com.kayhut.fuse.model.results.Property(NAME.type, "raw", DRAGON.name + i)))
                    .build();
            Relationship relationship1 = Relationship.Builder.instance()
                    .withRID("123")
                    .withDirectional(false)
                    .withEID1(entityA.geteID())
                    .withEID2("Dragon_" + i)
                    .withETag1("A")
                    .withETag2("B")
                    .withRType($ont.rType$(FIRE.getName()))
                    .build();

            Relationship relationship2 = Relationship.Builder.instance()
                    .withRID("123")
                    .withDirectional(false)
                    .withEID1(entityC.geteID())
                    .withEID2("Dragon_" + i)
                    .withETag1("C")
                    .withETag2("B")
                    .withRType($ont.rType$(FIRE.getName()))
                    .build();
            Assignment assignment = Assignment.Builder.instance().withEntity(entityA).withEntity(entityB).withEntity(entityC)
                    .withRelationship(relationship1).withRelationship(relationship2).build();
            builder.withAssignment(assignment);
        }

        return builder.build();

    }

    private void setCurrentPlan(PlanWithCost<Plan, PlanDetailedCost> currentPlan){
        EpbMockModule.plan = currentPlan;
    }

    protected boolean shouldIgnoreRelId() {
        return true;
    }
    private static FuseClient fuseClient;
    private static Ontology.Accessor $ont;
    private static SimpleDateFormat sdf;

    private static Function<Long, Function<Long, Function<Integer, Long>>> timestampValueFunctionFactory;
    private static Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;
    private static Function<Integer, Integer> temperatureValueFunction;
}
