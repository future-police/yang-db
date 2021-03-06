package com.yangdb.fuse.unipop.controller.promise.appender;

import com.yangdb.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.yangdb.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.StepDescriptor;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * Created by lior.perry on 29/03/2017.
 */
public class ElementConstraintSearchAppenderTest {
    @Test
    public void testNoConstraint() {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(!appendResult);
        Assert.assertTrue(searchBuilder.getQueryBuilder().getQuery() == null);
    }

    @Test
    public void testSimpleConstraint() throws JSONException {

        Traversal dragonTraversal = __.has("label", P.eq("dragon")).limit(100);

        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(Constraint.by(__.has(T.label, "dragon"))),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\n" +
                        "  \"bool\" : {\n" +
                        "    \"filter\" : [\n" +
                        "      {\n" +
                        "        \"bool\" : {\n" +
                        "          \"must\" : [\n" +
                        "            {\n" +
                        "              \"term\" : {\n" +
                        "                \"type\" : {\n" +
                        "                  \"value\" : \"dragon\",\n" +
                        "                  \"boost\" : 1.0\n" +
                        "                }\n" +
                        "              }\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"adjust_pure_negative\" : true,\n" +
                        "          \"boost\" : 1.0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"adjust_pure_negative\" : true,\n" +
                        "    \"boost\" : 1.0\n" +
                        "  }\n" +
                        "}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testComplexConstraint() throws JSONException {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(Constraint.by(__.and(__.has(T.label, "dragon"), __.has("name", "Drogar")))),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
"{\n" +
        "  \"bool\" : {\n" +
        "    \"filter\" : [\n" +
        "      {\n" +
        "        \"bool\" : {\n" +
        "          \"must\" : [\n" +
        "            {\n" +
        "              \"bool\" : {\n" +
        "                \"filter\" : [\n" +
        "                  {\n" +
        "                    \"bool\" : {\n" +
        "                      \"must\" : [\n" +
        "                        {\n" +
        "                          \"term\" : {\n" +
        "                            \"type\" : {\n" +
        "                              \"value\" : \"dragon\",\n" +
        "                              \"boost\" : 1.0\n" +
        "                            }\n" +
        "                          }\n" +
        "                        },\n" +
        "                        {\n" +
        "                          \"term\" : {\n" +
        "                            \"name\" : {\n" +
        "                              \"value\" : \"Drogar\",\n" +
        "                              \"boost\" : 1.0\n" +
        "                            }\n" +
        "                          }\n" +
        "                        }\n" +
        "                      ],\n" +
        "                      \"adjust_pure_negative\" : true,\n" +
        "                      \"boost\" : 1.0\n" +
        "                    }\n" +
        "                  }\n" +
        "                ],\n" +
        "                \"adjust_pure_negative\" : true,\n" +
        "                \"boost\" : 1.0\n" +
        "              }\n" +
        "            }\n" +
        "          ],\n" +
        "          \"adjust_pure_negative\" : true,\n" +
        "          \"boost\" : 1.0\n" +
        "        }\n" +
        "      }\n" +
        "    ],\n" +
        "    \"adjust_pure_negative\" : true,\n" +
        "    \"boost\" : 1.0\n" +
        "  }\n" +
        "}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }
}
