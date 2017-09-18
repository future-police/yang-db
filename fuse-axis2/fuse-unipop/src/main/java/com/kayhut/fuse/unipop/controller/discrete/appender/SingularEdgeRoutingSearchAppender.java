package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Set;

/**
 * Created by roman.margolis on 18/09/2017.
 */
public class SingularEdgeRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region Constructors
    public SingularEdgeRoutingSearchAppender(int maxNumRoutingValues) {
        this.maxNumRoutingValues = maxNumRoutingValues;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = SchemaUtil.getRelevantSingularEdgeSchemas(context);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End endSchema = context.getDirection().equals(Direction.OUT) ?
                edgeSchema.getSource().get() :
                edgeSchema.getDestination().get();

        if (endSchema.getRouting().isPresent()) {
            Set<String> routingValues =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> vertex.<String>value(endSchema.getRouting().get().getRoutingProperty().getName()))
                            .toJavaSet();

            if (routingValues.size() <= this.maxNumRoutingValues) {
                searchBuilder.getRouting().addAll(routingValues);
                return true;
            }
        }

        return false;
    }
    //endregion

    //region Fields
    private int maxNumRoutingValues;
    //endregion
}
