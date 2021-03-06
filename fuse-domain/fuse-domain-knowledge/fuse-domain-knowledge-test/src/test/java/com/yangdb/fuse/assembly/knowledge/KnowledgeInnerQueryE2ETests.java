package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.knowledge.domain.*;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.query.ParameterizedQuery;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.yangdb.fuse.model.query.properties.constraint.WhereByConstraint;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yangdb.fuse.assembly.knowledge.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.client.FuseClientSupport.*;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.assembly.knowledge.domain.RvalueBuilder._r;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.model.query.Rel.Direction.R;


public class KnowledgeInnerQueryE2ETests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4;
    static ValueBuilder v1, v2, v3, v4, v5, v6, v7, v8, v9, v10;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static RvalueBuilder rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8, rv9;

    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup();//Todo remove while running in Suite Context

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Entities for tests
        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .deleteTime(sdf.parse("2018-06-09 02:02:02.222"));
        e2 = _e(ctx.nextLogicalId()).cat("opel").ctx("context2").lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e3 = _e(ctx.nextLogicalId()).cat("opel").ctx("context3").lastUpdateUser("Kobi Shaul")
                .deleteTime(sdf.parse("2018-02-09 02:02:02.222"));
        e4 = _e(e3.logicalId).cat("mazda").ctx("context1").creationUser("Dudi Frid")
                .deleteTime(sdf.parse("2016-08-09 02:02:02.222"));
        // Evalue entities for tests
        v1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v2 = _v(ctx.nextValueId()).field("garage").value("Zion and his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz").creationTime(new Date(System.currentTimeMillis()));
        v3 = _v(ctx.nextValueId()).field("Car sales").value("chevrolet").bdt("California").ctx("Sale cars")
                .creationUser("Kobi Peretz").lastUpdateUser("Dudu Shaul").creationTime(sdf.parse("2013-03-20 12:12:35.111"));
        v4 = _v(ctx.nextValueId()).field("Garage").value(322).bdt("Netanya").ctx("fixing cars").creationUser("Haim Melamed")
                .lastUpdateUser("haim Melamed").creationTime(sdf.parse("2018-04-17 13:05:13.098"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987"));
        v5 = _v(ctx.nextValueId()).field("Color").value("White").bdt("Identifier").ctx("colors")
                .creationUser("Haim Melamed").creationTime(sdf.parse("2016-09-02 19:45:23.123"))
                .lastUpdateUser("haim Melamed").deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v6 = _v(ctx.nextValueId()).field("date meeting").value(sdf.parse("2015-02-03 14:04:33.125")).bdt("Car owners meeting")
                .ctx("Replacing between people").creationUser("Chip of cars").lastUpdateUser("Yachial Nadav")
                .creationTime(sdf.parse("2016-09-02 19:45:23.123")).deleteTime(sdf.parse("2017-12-12 01:00:00.000"));
        v7 = _v(ctx.nextValueId()).field("North Garages").value(222).bdt("North").ctx("North country")
                .creationUser("Gabi Levy").lastUpdateUser("Gabi Levy").creationTime(sdf.parse("2014-08-18 18:08:18.888"))
                .lastUpdateTime(sdf.parse("2018-05-07 03:51:52.387"));
        v8 = _v(ctx.nextValueId()).field("North Garages").value(sdf.parse("2013-01-01 11:01:31.121")).bdt("Car owners meeting")
                .ctx("changing information").creationUser("Yaaaaaariv").lastUpdateUser("Yael Biniamin")
                .creationTime(sdf.parse("2017-07-07 17:47:27.727")).deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v9 = _v(ctx.nextValueId()).field("North Garages").value(999).bdt("North").ctx("North country")
                .creationUser("Gabi Levy").lastUpdateUser("Gabi Levy").creationTime(sdf.parse("2014-08-18 18:08:18.888"))
                .lastUpdateTime(sdf.parse("2018-05-07 03:51:52.387"));
        v10 = _v(ctx.nextValueId()).field("conference date").value(sdf.parse("2013-01-01 11:01:31.121")).bdt("Car owners meeting")
                .ctx("changing information").creationUser("Yaaaaaariv").lastUpdateUser("Yael Biniamin")
                .creationTime(sdf.parse("2017-07-07 17:47:27.727")).deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        // Add Evalue to Entity
        e1.value(v1);
        e1.value(v2);
        e2.value(v3);
        e2.value(v4);
        e2.value(v5);
        e3.value(v6);
        e4.value(v7);
        e4.value(v8);
        e4.value(v9);
        e4.value(v10);

        // Relation entities for tests
        rel1 = _rel(ctx.nextRelId()).ctx("Car companies").cat("Cars").creationUser("Liat Plesner")
                .lastUpdateUser("Yael Pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-02-02 22:22:22.222"));
        rel2 = _rel(ctx.nextRelId()).ctx("Car Companies").cat("cars").creationUser("liat plesner")
                .lastUpdateUser("Yael pery").creationTime(sdf.parse("1990-00-00 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-05-03 19:19:19.192"));
        rel3 = _rel(ctx.nextRelId()).ctx("Number of wheels").cat("Wheels").creationUser("Liat Moshe")
                .lastUpdateUser("Yael pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:41.489")).deleteTime(sdf.parse("2010-09-09 19:19:11.999"));
        rel4 = _rel(ctx.nextRelId()).ctx("Quantity of wheels").cat("wheels").creationUser("Yaacov Gabuy")
                .lastUpdateUser("Meir Pery").creationTime(sdf.parse("1999-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:42.489")).deleteTime(sdf.parse("2008-08-08 88:88:88.888"));
        rel5 = _rel(ctx.nextRelId()).ctx("Quantity of Wheels").cat("Wheels").creationUser("Yaacov")
                .lastUpdateUser("Moshe").creationTime(sdf.parse("2009-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2006-06-07 05:45:55.565")).deleteTime(sdf.parse("2004-02-03 11:11:11.022"));
        // Rvalues for tests
        rv1 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Volvo").value(2018).bdt("manufacturer").ctx("Car company")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2018-07-12 09:01:03.764"));
        rv2 = _r(ctx.nextRvalueId()).relId(rv1.relationId).field("Audi").value(2025).bdt("Manufacturer").ctx("Car Company")
                .creationUser("kobi shaul").lastUpdateUser("Dudu Peretz").creationTime(sdf.parse("2019-09-02 10:51:53.563"))
                .deleteTime(sdf.parse("2019-09-02 10:51:53.564"));
        rv3 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Volvo").value("family").bdt("Manufacturer").ctx("Car company")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("1999-04-14 04:41:43.443"))
                .lastUpdateTime(sdf.parse("2018-04-17 23:59:58.987")).deleteTime(sdf.parse("2010-01-11 01:11:13.161"));
        rv4 = _r(ctx.nextRvalueId()).relId(rv1.relationId).field("audi").value(2025).bdt("Manufacturer").ctx("Cars Company")
                .creationUser("kobi Dudi shaul").lastUpdateUser("Dudi Peretz").creationTime(sdf.parse("2016-12-24 14:54:43.463"))
                .deleteTime(sdf.parse("2019-09-02 10:51:53.564"));
        rv5 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("volvo").value("expensive").bdt("Company").ctx("Car type")
                .creationUser("Avi Shaul").lastUpdateUser("Dudi Peretz").creationTime(sdf.parse("1981-04-21 13:21:53.003"))
                .lastUpdateTime(sdf.parse("2019-04-17 23:59:58.987")).deleteTime(sdf.parse("1985-07-10 01:11:13.161"));
        rv6 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("date").value(sdf.parse("2017-12-13 11:01:31.121"))
                .bdt("Purchase date").ctx("Date you bought the vehicle").creationUser("Avi Shaul").lastUpdateUser("Liran peretz")
                .creationTime(sdf.parse("1981-04-21 13:21:53.003")).deleteTime(sdf.parse("1985-07-10 01:11:13.161"));
        rv7 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Date").value(sdf.parse("2000-10-03 10:00:00.000"))
                .bdt("Purchase date").ctx("Date you sold the vehicle").creationUser("Avi Shaul").lastUpdateUser("Liran Peretz")
                .lastUpdateTime(sdf.parse("2018-09-17 23:59:58.987")).creationTime(sdf.parse("1983-08-17 17:27:57.707"))
                .deleteTime(sdf.parse("1987-07-16 06:16:16.166"));
        rv8 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Dodge").value("Family").bdt("company").ctx("Car Type")
                .creationUser("Gbi Levi").lastUpdateUser("Oron Lamed").creationTime(sdf.parse("2001-05-15 05:55:55.445"))
                .deleteTime(sdf.parse("2010-01-11 01:11:13.161"));
        rv9 = _r(ctx.nextRvalueId()).relId(ctx.nextRelId()).field("Dodge").value("Family").bdt("company").ctx("Car Type")
                .creationUser("Gbi Levi").lastUpdateUser("Oron Lamed").creationTime(sdf.parse("2001-05-15 05:55:55.445"))
                .deleteTime(sdf.parse("2010-01-11 01:11:13.161"));

        // Add Relation between two Entities and between Relation and Rvalue
        rel1.sideA(e1).sideB(e2).value(rv1, rv2, rv3);
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");

        rel2.sideA(e3).sideB(e4).value(rv4, rv5);
        e3.rel(rel2, "out");
        e4.rel(rel2, "in");

        rel3.sideA(e1).sideB(e3).value(rv6);
        e1.rel(rel3, "out");
        e3.rel(rel3, "in");

        // Insert Entity and Evalue entities to ES
        Assert.assertEquals("error loading data ",10, commit(ctx, INDEX, e1, e2, e3, e4));
        Assert.assertEquals("error loading data ",10, commit(ctx, INDEX, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
        Assert.assertEquals("error loading data ",5, commit(ctx, REL_INDEX, rel1, rel2, rel3, rel4, rel5));
        Assert.assertEquals("error loading data ",9, commit(ctx, REL_INDEX, rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8, rv9));
    }


    @AfterClass
    public static void after() {
        if(ctx!=null) Assert.assertEquals(34,ctx.removeCreated());
    }


    // Start Tests:
    @Test
    public void testSimpleFollowUpQueryWithConstraintOnId() throws IOException, InterruptedException, ParseException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E1", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, "category", WhereByConstraint.of(ConstraintOp.eq, "mazda")),
                        new Rel(3, "hasEvalue", R, null, 4, 0),
                        new ETyped(4, "V1", "Evalue", 0, 0),
                        new Rel(6, "relatedEntity", R, null, 7, 0),
                        new ETyped(7, "E2", "Entity", 9, 0),
                        new Rel(9, "hasEvalue", R, null, 10, 0),
                        new ETyped(10, "V2", "Evalue", 11, 0),
                        new EProp(11, "creationTime", WhereByConstraint.of(ConstraintOp.gt, "V1", "creationTime"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q1]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(7, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));
    }

    @Test
    public void testSimpleInnerQueryWithConstraintOnId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, e1.logicalId))
                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "id", InnerQueryConstraint.of(ConstraintOp.inSet, queryInner, "A", "id"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q1]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(3, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));

        Assert.assertEquals("call[q1]", pathResourceInfo.getResourceId());
        Assert.assertNotNull(pathResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(2, ((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(1)).get("entities")).size());

    }

    @Test
    public void testSimpleInnerQueryWithConstraintNEOnId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, e1.logicalId))
                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "id", InnerQueryConstraint.of(ConstraintOp.ne, queryInner, "A", "id"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q1]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(9, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));

        Assert.assertEquals("call[q1]", pathResourceInfo.getResourceId());
        Assert.assertNotNull(pathResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(10, ((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(1)).get("entities")).size());

    }

    @Test
    public void testSimpleInnerQueryWithConstraintEQOnId() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Evalue", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, e1.logicalId))
                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "id", InnerQueryConstraint.of(ConstraintOp.eq, queryInner, "A", "id"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));

        ParameterizedQuery query = (ParameterizedQuery) fuseClient.getQuery(graphResourceInfo.getV1QueryUrl(), ParameterizedQuery.class);
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[Entity:1]──Q[2]:{3|4}, \n" +
                        "                           └─?[3]:[category<eq,opel>], \n" +
                        "                           └-> Rel(hasEvalue:4)──Typ[Evalue:5]──?[6]:[id<eq,null>]]",
                QueryDescriptor.print(query));
        //region assert
        Assert.assertEquals("call[q1]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(3, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());

        QueryResourceInfo pathResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q1", "q1", queryOuter, new CreatePathsCursorRequest(new CreatePageRequest(100))));

        Assert.assertEquals("call[q1]", pathResourceInfo.getResourceId());
        Assert.assertNotNull(pathResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(2, ((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
        Assert.assertEquals(2, ((List) ((Map) (((List) ((Map) pathResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(1)).get("entities")).size());
        //endregion assert

    }

    @Test
    public void testSimpleInnerQueryWithConstraintOnField() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(5, "hasEvalue", R, null, 6, 0),
                        new ETyped(6, "V", "Evalue", 7, 0)


                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasEvalue", R, null, 5, 0),
                        new ETyped(5, "V", "Evalue", 6, 0),
                        new EProp(6, "fieldId", InnerQueryConstraint.of(ConstraintOp.inSet, queryInner, "V", "fieldId"))
                )).build();


        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q2", "q2", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q2]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(9, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
    }

    @Test
    public void testSimpleInnerQueryWithConstraintOnMultipleFields() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        Query queryInner = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(5, "hasEvalue", R, null, 6, 0),
                        new ETyped(6, "V", "Evalue", 7, 0)


                )).build();

        Query queryOuter = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "E", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new EProp(4, "id", InnerQueryConstraint.of(ConstraintOp.inSet, queryInner, "A", "id")),
                        new Rel(5, "hasEvalue", R, null, 6, 0),
                        new ETyped(6, "V", "Evalue", 7, 0),
                        new EProp(7, "fieldId", InnerQueryConstraint.of(ConstraintOp.inSet, queryInner, "V", "fieldId"))
                )).build();

        QueryResourceInfo graphResourceInfo = query(fuseClient, fuseResourceInfo,
                new CreateQueryRequest("q2", "q2", queryOuter, new CreateGraphCursorRequest(new CreatePageRequest(100))));


        Assert.assertEquals("call[q2]", graphResourceInfo.getResourceId());
        Assert.assertNotNull(graphResourceInfo.getCursorStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageStoreUrl());
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0));
        Assert.assertFalse(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0));
        Assert.assertNotNull(graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertNotNull(((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"));
        Assert.assertEquals(1, ((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments")).size());
        Assert.assertEquals(9, ((List) ((Map) (((List) ((Map) graphResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).get("assignments"))).get(0)).get("entities")).size());
    }

}
