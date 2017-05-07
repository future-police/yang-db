package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 23/04/2017.
 */
public class SearchPromiseVertexControllerTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {

        client = mock(Client.class);

        //mock response with 2 layers of aggregations
        SearchResponse responseMock = mock(SearchResponse.class);

        Aggregations aggregations = mock(Aggregations.class);

        Terms.Bucket destBucket = mock(Terms.Bucket.class);
        when(destBucket.getKeyAsString()).thenReturn("destination1");
        when(destBucket.getDocCount()).thenReturn(1000L);

        Terms.Bucket sourceBucket = mock(Terms.Bucket.class);
        when(sourceBucket.getKeyAsString()).thenReturn("source1");
        when(sourceBucket.getDocCount()).thenReturn(1L);
        when(sourceBucket.getAggregations()).thenReturn(aggregations);

        Terms sourceLayer = mock(Terms.class);
        when(sourceLayer.getBuckets()).thenReturn(Arrays.asList(sourceBucket));

        Terms destLayer = mock(Terms.class);
        when(destLayer.getBuckets()).thenReturn(Arrays.asList(destBucket));

        Map map = new HashMap();
        map.put(PromiseEdgeConstants.SOURCE_AGGREGATION_LAYER,sourceLayer);
        map.put(PromiseEdgeConstants.DEST_AGGREGATION_LAYER,destLayer);
        when(aggregations.asMap()).thenReturn(map);

        when(responseMock.getAggregations()).thenReturn(aggregations);

        ListenableActionFuture<SearchResponse> futureMock = mock(ListenableActionFuture.class);
        when(futureMock.actionGet()).thenReturn(responseMock);

        SearchRequestBuilder searchRequestBuilderMock = mock(SearchRequestBuilder.class);
        when(searchRequestBuilderMock.execute()).thenReturn(futureMock);
        when(searchRequestBuilderMock.setQuery(org.mockito.Matchers.any(QueryBuilder.class))).then((query) -> {
            // validation logic
           return searchRequestBuilderMock;
        });
        when(searchRequestBuilderMock.setSearchType(SearchType.COUNT)).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.setScroll(any(TimeValue.class))).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.setSize(anyInt())).thenReturn(searchRequestBuilderMock);
        when(searchRequestBuilderMock.execute()).thenReturn(futureMock);
        when(client.prepareSearch()).thenReturn(searchRequestBuilderMock);

        configuration = mock(ElasticGraphConfiguration.class);
    }

    @Test
    public void testSingleIdPromiseVertexWithoutConstraint() throws ExecutionException, InterruptedException {

        UniGraph graph = mock(UniGraph.class);

        //basic edge constraint
        Traversal constraint = __.and(__.has(T.label, "fire"), __.has("direction", "out"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("constraint", P.eq(Constraint.by(constraint)))));

        //create vertices to start from
        Vertex startVertex1 = mock(Vertex.class);
        when(startVertex1.id()).thenReturn("3");
        when(startVertex1.label()).thenReturn("dragon");

        Vertex startVertex2 = mock(Vertex.class);
        when(startVertex2.id()).thenReturn("13");
        when(startVertex2.label()).thenReturn("dragon");

        SearchVertexQuery searchQuery = mock(SearchVertexQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Edge.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);
        when(searchQuery.getVertices()).thenReturn(Arrays.asList(startVertex1, startVertex2));

        //prepare schema provider
        IndexPartition indexPartition = mock(IndexPartition.class);
        when(indexPartition.getIndices()).thenReturn(Arrays.asList("v1"));
        GraphEdgeSchema edgeSchema = mock(GraphEdgeSchema.class);
        when(edgeSchema.getIndexPartition()).thenReturn(indexPartition);
        GraphElementSchemaProvider schemaProvider = mock(GraphElementSchemaProvider.class);
        when(schemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(edgeSchema));

        SearchPromiseVertexController controller = new SearchPromiseVertexController(client, configuration, graph, schemaProvider);

        List<Edge> edges = Stream.ofAll(() -> controller.search(searchQuery)).toJavaList();

        edges.forEach(edge -> System.out.println(edge));

        Assert.assertEquals(1, edges.size());
        Assert.assertEquals(1000L, edges.get(0).property(PromiseEdgeConstants.PROMISE_EDGE_COUNT_PROP).value());

    }
}
