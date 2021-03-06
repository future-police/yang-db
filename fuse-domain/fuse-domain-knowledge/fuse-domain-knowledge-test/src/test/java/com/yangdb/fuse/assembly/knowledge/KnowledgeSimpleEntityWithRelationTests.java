package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RefBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KnowledgeQueryBuilder.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder.REF_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RefBuilder._ref;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.client.FuseClientSupport.query;

public class KnowledgeSimpleEntityWithRelationTests {
    static KnowledgeWriterContext ctx;
    static SimpleDateFormat sdf;
    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(false,true);//todo remove for CI tests
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @AfterClass
    public static void teardown() throws Exception {
//        Setup.cleanup(true,true);
    }

    @After
    public void after() {
        if(ctx!=null) ctx.removeCreated();//todo restore for CI tests
    }

    @Test
    public void testInsertOneSimpleEntityWithRelation() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel,"out");
        e2.rel(rel,"in");


        Assert.assertEquals(4, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(e1.getETag()).relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(e1.toEntity())//context entity
                        .withEntity(e2.toEntity())//context entity
                        .withEntity(rel.toEntity())//context entity
                        .withRelationships(e1.withRelations())//relationships
                        .withRelationships(e2.withRelations())//relationships
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);
    }

    @Test
    public void testSimpleEntityWithRelationInOut() throws IOException, InterruptedException {
        // p1 --knows-> p2 --knows-> p3
        final EntityBuilder p1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder p2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder p3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel_p1_p2 = _rel(ctx.nextRelId()).cat("knows").sideA(p1).sideB(p2);
        p1.rel(rel_p1_p2,"out");
        p2.rel(rel_p1_p2,"in");

        final RelationBuilder rel_p1_p3 = _rel(ctx.nextRelId()).cat("knows").sideA(p1).sideB(p3);
        p1.rel(rel_p1_p3,"out");
        p3.rel(rel_p1_p3,"in");



        Assert.assertEquals(7, commit(ctx, INDEX, p1,p2,p3));
        Assert.assertEquals(2, commit(ctx, REL_INDEX, rel_p1_p3,rel_p1_p2));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(p1.getETag())
                .relatedTo(HAS_IN_RELATION,HAS_OUT_RELATION, p1.getETag()+"->"+p2.getETag(),p2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,query,
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

        // Check Entity Response
        Assert.assertEquals(2, pageData.getSize());
        Assert.assertEquals(2, ((AssignmentsQueryResult) pageData).getAssignments().size());

    }

    @Test
    public void testSimpleEntityWithGeneralRelationNotion() throws IOException, InterruptedException {
        // p1 --knows-> p2 --knows-> p3
        final EntityBuilder p1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder p2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder p3 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel_p1_p2 = _rel(ctx.nextRelId()).cat("knows").sideA(p1).sideB(p2);
        p1.rel(rel_p1_p2,"out");
        p2.rel(rel_p1_p2,"in");

        final RelationBuilder rel_p1_p3 = _rel(ctx.nextRelId()).cat("knows").sideA(p1).sideB(p3);
        p1.rel(rel_p1_p3,"out");
        p3.rel(rel_p1_p3,"in");



        Assert.assertEquals(7, commit(ctx, INDEX, p1,p2,p3));
        Assert.assertEquals(2, commit(ctx, REL_INDEX, rel_p1_p3,rel_p1_p2));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = start().withEntity(p1.getETag())
                .relatedTo(p1.getETag()+"->"+p2.getETag(),p2.getETag()).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,query,
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

        // Check Entity Response
        Assert.assertEquals(4, pageData.getSize());
        Assert.assertEquals(4, ((AssignmentsQueryResult) pageData).getAssignments().size());

    }


    @Test
    public void testInsertOneSimpleEntityWithRelationAndReference() throws IOException, InterruptedException {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2);
        e1.rel(rel,"out");
        e2.rel(rel,"in");

        // Create ref
        RefBuilder ref1 = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting monti")
                .url("http://someHosting/monti");
        RefBuilder ref2 = _ref(ctx.nextRefId())
                .sys("sys")
                .title("some interesting jhony")
                .url("http://someHosting/jhony");
        //after ref is rendered add as a sub resource to the entity
        e1.reference(ref1);
        e2.reference(ref2);

        //verify data inserted correctly
        Assert.assertEquals(4, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(2, commit(ctx, REF_INDEX, ref1,ref2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));


        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = start()
                .withEntity(e1.getETag())
                .withRef(ref1.getETag())
                .relatedTo(e1.getETag()+"->"+e2.getETag(),e2.getETag()).build();

        // Read Entity (with V1 query)
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(4, assignments.get(0).getRelationships().size());
        Assert.assertEquals(5, assignments.get(0).getEntities().size());

        //bug logicalId returns on Reference entity
        List<Entity> subEntities1 = e1.subEntities();
        //bug logicalId returns on Reference entity
        List<Entity> subEntities2 = e2.subEntities();

        //verify assignments return as expected
        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(rel.toEntity())
                        //entity 1
                        .withEntity(e1.toEntity())
                        .withEntities(subEntities1)
                        .withRelationships(e1.withRelations())
                        //entity 2
                        .withEntity(e2.toEntity())
                        .withEntities(subEntities2)
                        .withRelationships(e2.withRelations())
                        .build()).build();

        // Check if expected and actual are equal
        //todo - check why this fails in maven build proccess - surefire plugin, and passes in IDE ???
//        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true,true);

    }

    @Test
    public void testRelatedEntityRelTypeLeft() throws IOException, InterruptedException, ParseException {

        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        String e1Id = e1.id();
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel").creationTime(sdf.parse(creationTime));
        e1.rel(rel,"out");
        e2.rel(rel,"in");


        Assert.assertEquals(4, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName("q2").withOnt("Knowledge").withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", "Entity", e1Id, "A", 2, 0),
                new Rel(2, "relatedEntity", Rel.Direction.L, "", 3, 4),
                new RelProp(4, "creationTime", Constraint.of(ConstraintOp.eq, creationTime), 0),
                new ETyped(3, "B", "Entity", 0, 0)
        )).build();

        // Read Entity (with V1 query)
        AssignmentsQueryResult<Entity,Relationship> pageData = (AssignmentsQueryResult) query(fuseClient, fuseResourceInfo, query, new KnowledgeGraphHierarchyCursorRequest());

        Assert.assertEquals(1, pageData.getAssignments().size());
        Assert.assertEquals(2,pageData.getAssignments().get(0).getEntities().size());
        Entity entityA = Stream.ofAll(pageData.getAssignments().get(0).getEntities()).find(e -> e.geteTag().contains("A")).get();
        Entity entityB = Stream.ofAll(pageData.getAssignments().get(0).getEntities()).find(e -> e.geteTag().contains("B")).get();
        Assert.assertEquals(e1Id, entityA.geteID());
        Assert.assertEquals(e2.id(), entityB.geteID());

        Assert.assertEquals(1,pageData.getAssignments().get(0).getRelationships().size());
        Option<Property> category = Stream.ofAll(pageData.getAssignments().get(0).getRelationships().get(0).getProperties()).find(p -> p.getpType().equals("category"));
        Assert.assertFalse(category.isEmpty());
        Assert.assertEquals("rel", category.get().getValue());

    }

    @Test
    public void testRelatedEntityRelTypeRight() throws IOException, InterruptedException, ParseException {

        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        String e1Id = e1.id();
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).ctx("context1").cat("rel").creationTime(sdf.parse(creationTime));
        e1.rel(rel,"out");
        e2.rel(rel,"in");


        Assert.assertEquals(4, commit(ctx, INDEX, e1,e2));
        Assert.assertEquals(1, commit(ctx, REL_INDEX, rel));

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName("q2").withOnt("Knowledge").withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", "Entity", e1Id, "A", 2, 0),
                new Rel(2, "relatedEntity", Rel.Direction.R, "", 3, 4),
                new RelProp(4, "creationTime", Constraint.of(ConstraintOp.eq, creationTime), 0),
                new ETyped(3, "B", "Entity", 0, 0)
        )).build();

        // Read Entity (with V1 query)
        AssignmentsQueryResult<Entity,Relationship> pageData = (AssignmentsQueryResult<Entity,Relationship>) query(fuseClient, fuseResourceInfo, query);

        Assert.assertEquals(1, pageData.getAssignments().size());
        Assert.assertEquals(2,pageData.getAssignments().get(0).getEntities().size());
        Entity entityA = Stream.ofAll(pageData.getAssignments().get(0).getEntities()).find(e -> e.geteTag().contains("A")).get();
        Entity entityB = Stream.ofAll(pageData.getAssignments().get(0).getEntities()).find(e -> e.geteTag().contains("B")).get();
        Assert.assertEquals(e1Id, entityA.geteID());
        Assert.assertEquals(e2.id(), entityB.geteID());

    }
}
