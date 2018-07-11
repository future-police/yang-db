package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeDataInfraManager;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.kayhut.fuse.utils.FuseClient;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by rani on 5/2/2018.
 */
@Ignore
public class KnowledgeOntologySimpleE2ETest {
    static KnowledgeDataInfraManager manager;
    static FuseClient fuseClient;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        manager.load();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Setup.cleanup();
    }

    private AssignmentsQueryResult GetAssignmentForQuery(Query query, FuseResourceInfo resourceInfo, int sleeptime, int cursorType) throws IOException, InterruptedException {
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(resourceInfo.getQueryStoreUrl(), query);
        CreateCursorRequest cursorRequest = null;

        switch (cursorType) {
            case 0:
                cursorRequest = new CreateGraphCursorRequest();
                break;
            case 1:
                cursorRequest = new CreatePathsCursorRequest();
                break;
            default:
                cursorRequest = new CreateGraphCursorRequest();
                break;
        }

        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(sleeptime);
            }
        }

        return (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    @Test
    public void ComplexQueryTest() throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("ComplexQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEntityReference"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Reference"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11), 0),
                new EProp(10, $ont.pType$("url"), Constraint.of(ConstraintOp.like, "*clown")),
                new EProp(11, $ont.pType$("title"), Constraint.of(ConstraintOp.notEmpty)/*, "sample")*/))
        ).build();

        AssignmentsQueryResult pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        int resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize, 1);
        String rtype = pageData.getResultType();
        for (int i = 0; i < resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount, 2);
            Assert.assertEquals(relationsCount, 1);
        }

        query = Query.Builder.instance().withName("ComplexQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Nic*")),
                new EProp(11, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize, 1);
        rtype = pageData.getResultType();
        for (int i = 0; i < resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount, 4);
            Assert.assertEquals(relationsCount, 2);
        }

        query = Query.Builder.instance().withName("ComplexQuery2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7, 13), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "Nic*")),
                new EProp(11, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(13, $ont.rType$("hasEntityReference"), R, null, 14, 0),
                new ETyped(14, "C", $ont.eType$("Reference"), 15, 0),
                new Quant1(15, QuantType.all, Arrays.asList(16, 17), 0),
                new EProp(16, $ont.pType$("url"), Constraint.of(ConstraintOp.eq, "http://1.1.1.1:6200/circus")),
                new EProp(17, $ont.pType$("creationTime"), Constraint.of(ConstraintOp.gt, "2018-01-01 00:00:00.000"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        resultsSize = pageData.getSize();
        Assert.assertEquals(resultsSize, 1);
        rtype = pageData.getResultType();
        for (int i = 0; i < resultsSize; i++) {
            int entitiesCount = pageData.getAssignments().get(i).getEntities().size();
            int relationsCount = pageData.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(entitiesCount, 3);
            Assert.assertEquals(relationsCount, 2);
        }
    }

    private void CheckAssignmentQueryResults(int total, int relationsCount, int entitiesCount, AssignmentsQueryResult data) {
        int resultsSize = data.getSize();
        Assert.assertEquals(resultsSize, total);
        String rtype = data.getResultType();
        for (int i = 0; i < resultsSize; i++) {
            int tmpEntitiesCount = data.getAssignments().get(i).getEntities().size();
            int tmpRelationsCount = data.getAssignments().get(i).getRelationships().size();
            Assert.assertEquals(tmpEntitiesCount, entitiesCount);
            Assert.assertEquals(tmpRelationsCount, relationsCount);
        }
    }

    @Test
    public void SimpleQueryTest() throws IOException, InterruptedException {
        //FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        // Fit the query to the sample data
        /*Query query = Query.Builder.instance().withName("SimpleQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("LogicalEntity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(101, 3), 0),
                new EProp(101, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(3, $ont.rType$("hasEntity"), R, null, 4, 0),
                new ETyped(4, "B", $ont.eType$("Entity"), 5, 0),
                new Quant1(5, QuantType.all, Arrays.asList(6), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context2")))
        ).build();*/

        Query query = Query.Builder.instance().withName("SimpleQuery").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(10, 11, 12), 0),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, "Nick1")),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        AssignmentsQueryResult pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 1, 2, pageData);

        query = Query.Builder.instance().withName("SimpleQuery1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEvalue"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Evalue"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(11, 12), 0),
                new EProp(11, $ont.pType$("bdt"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(12, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 2, 4, pageData);

        query = Query.Builder.instance().withName("SimpleQuery2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasEntityReference"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Reference"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(11, 12), 0),
                new EProp(11, $ont.pType$("system"), Constraint.of(ConstraintOp.eq, "system7")),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Arla Nava"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 1, 2, pageData);

        query = Query.Builder.instance().withName("SimpleQuery3").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasInsight"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Insight"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(12), 0),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Eve"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 2, 3, pageData);

        query = Query.Builder.instance().withName("SimpleQuery4").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasRelation"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Relation"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(12), 0),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Shani"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 2, 3, pageData);

        query = Query.Builder.instance().withName("SimpleQuery5").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new Rel(7, $ont.rType$("hasRelation"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Relation"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(12, 13), 0),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Shani")),
                new Rel(13, $ont.rType$("hasRvalue"), R, null, 14, 0),
                new ETyped(14, "C", $ont.eType$("Rvalue"), 15, 0),
                new Quant1(15, QuantType.all, Arrays.asList(16), 0),
                new EProp(16, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "sum"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 3, 4, pageData);

        query = Query.Builder.instance().withName("SimpleQuery6").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(4, 5, 6, 7), 0),
                new EProp(4, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(5, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "person")),
                new EProp(6, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Hassan")),
                new Rel(7, $ont.rType$("hasRelation"), R, null, 8, 0),
                new ETyped(8, "B", $ont.eType$("Relation"), 9, 0),
                new Quant1(9, QuantType.all, Arrays.asList(12, 13), 0),
                new EProp(12, $ont.pType$("creationUser"), Constraint.of(ConstraintOp.eq, "Shani")),
                new Rel(13, $ont.rType$("hasRvalue"), R, null, 14, 0),
                new ETyped(14, "C", $ont.eType$("Rvalue"), 15, 0),
                new Quant1(15, QuantType.all, Arrays.asList(16), 0),
                new EProp(16, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "sum"))
        )).build();

        pageData = GetAssignmentForQuery(query, fuseResourceInfo, 10, 0);
        CheckAssignmentQueryResults(1, 2, 3, pageData);
    }

}