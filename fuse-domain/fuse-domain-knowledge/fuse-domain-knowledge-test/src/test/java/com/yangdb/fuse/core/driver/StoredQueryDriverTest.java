package com.yangdb.fuse.core.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.yangdb.fuse.assembly.knowledge.Setup;
import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.FileBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.*;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.*;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest.CsvElement;
import com.yangdb.fuse.model.transport.cursor.CreateCsvCursorRequest.ElementType;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.jooby.internal.RequestScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.empty;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.notEmpty;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.*;

public class StoredQueryDriverTest extends BaseModuleInjectionTest {
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static KnowledgeWriterContext ctx;
    static FileBuilder f1, f2;

    @BeforeClass
    public static void setupTest() throws Exception {
//        Setup.setup();
    }

    public void setupData(Client client, RawSchema schema) throws ParseException, JsonProcessingException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, schema);
        // Efile entities for tests
        String logicalId = ctx.nextLogicalId();
        f1 = _f(ctx.nextFileId()).logicalId(logicalId).name("mazda").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"));
        f2 = _f(ctx.nextFileId()).logicalId(logicalId).name("subaru").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        Assert.assertEquals(2, commit(ctx, INDEX, f1, f2));
    }

    @Test
    public void testGetInfoNoQueries() {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();
        final Optional<StoreResourceInfo> info = driver.getInfo();

        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));
    }

    @Test
    public void testGetInfoWithQuery() {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("q10").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "name", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q10", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        final Optional<StoreResourceInfo> info = driver.getInfo();
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));
        Assert.assertTrue(info.get().getResourceUrls().iterator().hasNext());

    }

    @Test
    public void testGetNonDeletedInfoWithQuery() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        Query query = Query.Builder.instance().withName("q10").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "deleteTime", new OptionalUnaryParameterizedConstraint(empty, new HashSet<>(Arrays.asList(empty, notEmpty)),
                                new NamedParameter("deleteTime"))),
                        new EProp(4, "name", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q10", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        //force indices refresh
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        final Optional<StoreResourceInfo> info = driver.getInfo();
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));
        Assert.assertTrue(info.get().getResourceUrls().iterator().hasNext());

        Optional<QueryResourceInfo> infoCall = driver.call(new ExecuteStoredQueryRequest("callQ1", "q10",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Collections.singleton(new NamedParameter("name", Arrays.asList("mazda", "subaru"))),
                Collections.EMPTY_LIST));

        Assert.assertTrue(infoCall.isPresent());
        Assert.assertFalse(infoCall.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());

        List<Assignment<Entity, Relationship>> assignments = ((AssignmentsQueryResult<Entity, Relationship>) infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments();
        Assert.assertFalse(assignments.isEmpty());
        List<Entity> entities = new ArrayList<>(assignments.get(0).getEntities());
        Assert.assertFalse(entities.isEmpty());
        Assert.assertEquals(1, entities.size());
        Assert.assertFalse(entities.get(0).getProperties().stream().anyMatch(p->p.getpType().equals("deleteTime")));

    }
    @Test
    public void testGetDeletedInfoWithQuery() {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("q10").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "deleteTime", new OptionalUnaryParameterizedConstraint(empty, new HashSet<>(Arrays.asList(empty, notEmpty)),
                                new NamedParameter("deleteTime"))),
                        new EProp(4, "name", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q10", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());
        //force indices refresh
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        final Optional<StoreResourceInfo> info = driver.getInfo();
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));
        Assert.assertTrue(info.get().getResourceUrls().iterator().hasNext());

        Optional<QueryResourceInfo> infoCall = driver.call(new ExecuteStoredQueryRequest("callQ1", "q10",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Arrays.asList(new NamedParameter("name", Arrays.asList("mazda", "subaru")),new NamedParameter("deleteTime", notEmpty)),
                Collections.EMPTY_LIST));

        Assert.assertTrue(infoCall.isPresent());
        Assert.assertFalse(infoCall.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        List<Assignment<Entity, Relationship>> assignments = ((AssignmentsQueryResult<Entity, Relationship>) infoCall.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments();

        List<Entity> entities = new ArrayList<>(assignments.get(0).getEntities());
        Assert.assertFalse(assignments.isEmpty());
        Assert.assertFalse(entities.isEmpty());
        Assert.assertEquals(1, entities.size());
        Assert.assertTrue(entities.get(0).getProperties().stream().anyMatch(p->p.getpType().equals("deleteTime")));
    }

    @Test
    public void testCreateAndFetchSingleValueNoParams() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, f1.logicalId))
                )).build();

        final CreateQueryRequest createQueryRequest = new CreateQueryRequest("q1", "MyQuery", query, new PlanTraceOptions(),
                new CreatePathsCursorRequest(new CreatePageRequest()));

        final Optional<QueryResourceInfo> info = driver.create(createQueryRequest);
        Assert.assertTrue(info.isPresent());
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(2, ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);

    }

    @Test
    public void testCreateAndFetchSingleValueNoParamsWithPage() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, f1.logicalId))
                )).build();

        final CreateQueryRequest createQueryRequest = new CreateQueryRequest("q1", "MyQuery", query, new PlanTraceOptions(),
                new CreatePathsCursorRequest(new CreatePageRequest(1)));

        final Optional<QueryResourceInfo> info = driver.create(createQueryRequest);
        Assert.assertTrue(info.isPresent());
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(1, ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);

        Optional<Object> data = driver.getNextPageData("q1", Optional.empty(), 1, true);
        Assert.assertTrue(data.isPresent());
        Assert.assertFalse(((AssignmentsQueryResult<Entity,Relationship>) data.get()).getAssignments().isEmpty());
        Assert.assertEquals(1, ((AssignmentsQueryResult<Entity,Relationship>) data.get()).getAssignments().get(0).getEntities().size(), 1);

        data = driver.getNextPageData("q1", Optional.empty(), 1, true);
        Assert.assertTrue(data.isPresent());
        Assert.assertTrue(((AssignmentsQueryResult) data.get()).getAssignments().isEmpty());
    }

    @Test
    @Ignore
    //todo fix plan builder for this given query
    public void testCallAndFetchMultiValueOnRel() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        String e1Id = e1.id();
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel").creationTime(sdf.parse(creationTime));
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("bell").creationTime(sdf.parse(creationTime));
        e1.rel(rel2, "out");
        e2.rel(rel2, "in");


        Assert.assertEquals(6, commit(ctx, INDEX, e1, e2));
        Assert.assertEquals(2, commit(ctx, REL_INDEX, rel1, rel2));

        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName("q1").withOnt("Knowledge").withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", "Entity", e1Id, "A", 2, 0),
                new Rel(2, "relatedEntity", Rel.Direction.L, "", 6, 3),
                new Quant1(3, QuantType.all, Arrays.asList(4, 5), 0),
                new RelProp(4, "creationTime", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("creationTime")), 0),
                new RelProp(5, "category", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("category")), 0),
                new ETyped(6, "B", "Entity", 0, 0)
        )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(
                new CreateQueryRequest("q1", "myStoredQuery", query)
                        .storageType(CreateQueryRequest.StorageType._stored)
                        .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Arrays.asList(new NamedParameter("creationTime", creationTime),
                        new NamedParameter("category", Arrays.asList("bell", "dell"))),
                Collections.EMPTY_LIST));

        // Read Entity (with V1 query)
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assignment<Entity,Relationship> assignment = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0);
        com.yangdb.fuse.model.results.Entity entityA = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("A")).get();

        Entity entityB = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("B")).get();
        Assert.assertEquals(e1Id, entityA.geteID());
        Assert.assertEquals(e2.id(), entityB.geteID());

        List<Relationship> relationships = new ArrayList<>(assignment.getRelationships());
        Assert.assertEquals(1, relationships.size());
        Option<Property> category = Stream.ofAll(relationships.get(0).getProperties()).find(p -> p.getpType().equals("category"));
        Assert.assertFalse(category.isEmpty());
        Assert.assertEquals("rel", category.get().getValue());

    }

    @Test
    public void testCallAndFetchSingleParamInSetConstraint() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("q1").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "name", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Collections.singleton(new NamedParameter("name", Arrays.asList("mazda", "subaru"))),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(2, ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);
    }

    @Test
    public void testCallAndFetchSingleParamInGroupConstraint() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("q1").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EPropGroup(2, new EProp(3, "name",
                                ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name"))))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Collections.singleton(new NamedParameter("name", "mazda")),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(1, ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);
    }

    @Test
    public void testCallAndFetchMultiParamsWithCursor() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "logicalId", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("logicalId"))),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, "family cars")),
                        new EProp(5, "name", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query,
                new CreateCsvCursorRequest(new CsvElement[]{
                        new CsvElement("A", "logicalId", ElementType.Entity),
                        new CsvElement("A", "context", ElementType.Entity),
                        new CsvElement("A", "name", ElementType.Entity)
                }).withHeaders(true).with(new CreatePageRequest()))
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                Arrays.asList(new NamedParameter("logicalId", f1.logicalId), new NamedParameter("name", "mazda")),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines().length == 0);
        Assert.assertEquals(3, ((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines()[0].split(",").length);

        Optional<Boolean> deleted = driver.delete("callQ1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("callQ1");
        Assert.assertFalse(deleted.get());

        deleted = driver.delete("q1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("q1");
        Assert.assertFalse(deleted.get());

    }

    @Test
    public void testCallAndFetchMultiParamsWithCursorInCallingRequest() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "logicalId", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("logicalId"))),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, "family cars")),
                        new EProp(5, "name", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateCsvCursorRequest(new CsvElement[]{
                        new CsvElement("A", "logicalId", ElementType.Entity),
                        new CsvElement("A", "context", ElementType.Entity),
                        new CsvElement("A", "name", ElementType.Entity)
                }).withHeaders(true).with(new CreatePageRequest()),
                Arrays.asList(new NamedParameter("logicalId", f1.logicalId), new NamedParameter("name", "mazda")),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines().length == 0);
        Assert.assertEquals(3, ((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines()[0].split(",").length);

        Optional<Boolean> deleted = driver.delete("callQ1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("callQ1");
        Assert.assertFalse(deleted.get());

        deleted = driver.delete("q1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("q1");
        Assert.assertFalse(deleted.get());

    }

    @Test
    public void testCallAndFetchMultiParamsWithCursorInCallingOverridingRequest() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "logicalId", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("logicalId"))),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, "family cars")),
                        new EProp(5, "name", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query,
                new CreateGraphCursorRequest(new CreatePageRequest()))
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateCsvCursorRequest(new CsvElement[]{
                        new CsvElement("A", "logicalId", ElementType.Entity),
                        new CsvElement("A", "context", ElementType.Entity),
                        new CsvElement("A", "name", ElementType.Entity)
                }).withHeaders(true).with(new CreatePageRequest()),
                Arrays.asList(new NamedParameter("logicalId", f1.logicalId), new NamedParameter("name", "mazda")),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines().length == 0);
        Assert.assertEquals(3, ((CsvQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getCsvLines()[0].split(",").length);

        Optional<Boolean> deleted = driver.delete("callQ1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("callQ1");
        Assert.assertFalse(deleted.get());

        deleted = driver.delete("q1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("q1");
        Assert.assertFalse(deleted.get());

    }

    @Test
    public void testCallAndFetchMultiParams() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "logicalId", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("logicalId"))),
                        new EProp(4, "context", Constraint.of(ConstraintOp.eq, "family cars")),
                        new EProp(5, "name", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new CreateQueryRequest("q1", "myStoredQuery", query)
                .storageType(CreateQueryRequest.StorageType._stored)
                .type(parameterized));
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                new CreateGraphCursorRequest(new CreatePageRequest()),
                Arrays.asList(new NamedParameter("logicalId", f1.logicalId), new NamedParameter("name", "mazda")),
                Collections.EMPTY_LIST));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(1, ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);
        Assert.assertTrue(((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().get(0).getProperties().contains(new Property("name", "raw", "mazda")));

        Optional<Boolean> deleted = driver.delete("callQ1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("callQ1");
        Assert.assertFalse(deleted.get());

        deleted = driver.delete("q1");
        Assert.assertTrue(deleted.get());
        deleted = driver.delete("q1");
        Assert.assertFalse(deleted.get());

    }
}
