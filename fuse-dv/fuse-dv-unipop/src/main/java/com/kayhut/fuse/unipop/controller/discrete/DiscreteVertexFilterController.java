package com.kayhut.fuse.unipop.controller.discrete;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.VertexControllerBase;
import com.kayhut.fuse.unipop.controller.common.appender.*;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.discrete.context.DiscreteVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.converter.DiscreteVertexFilterConverter;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.appender.SizeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProvider;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import com.kayhut.fuse.unipop.predicates.SelectP;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.kayhut.fuse.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 26/09/2017.
 */
public class DiscreteVertexFilterController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexFilterController(Client client, ElasticGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE_FILTER));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.orderProviderFactory = orderProviderFactory;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if(constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        return filterVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit()));

        CompositeSearchAppender<CompositeControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new SizeSearchAppender(configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new FilterBulkSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new FilterRoutingSearchAppender()),
                        wrap(new FilterIndexSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender("type")),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, true);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(), searchBuilder.getScrollTime());

        ElementConverter<SearchHit, Edge> converter = new DiscreteVertexFilterConverter(context);

        return Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();
    }
    //endregion


    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private SearchOrderProviderFactory orderProviderFactory;
    private Client client;
    private ElasticGraphConfiguration configuration;
    //endregion
}
