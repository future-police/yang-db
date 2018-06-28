package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.AndStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Elad on 4/26/2017.
 */
public class ConstraintSearchAppender implements SearchAppender<CompositeControllerContext> {
    //region ElementControllerContext Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, CompositeControllerContext context) {
        Set<String> labels = getContextRelevantLabels(context);

        Traversal newConstraint = context.getConstraint().isPresent() ?
                context.getConstraint().get().getTraversal().asAdmin().clone() :
                __.has(T.label, P.within(labels));

        List<GraphElementConstraint> elementConstraints =
                    context.getElementType().equals(ElementType.vertex) ?
                            Stream.ofAll(labels)
                                    .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                                    .map(GraphElementSchema::getConstraint)
                                    .toJavaList() :
                            Stream.ofAll(context.getSchemaProvider().getEdgeSchemas(
                                    Stream.ofAll(context.getBulkVertices()).get(0).label(),
                                    context.getDirection(),
                                    Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor())).get(0)))
                                    .map(GraphElementSchema::getConstraint)
                                    .toJavaList();

        if (!elementConstraints.isEmpty()) {
            List<HasStep> labelHasSteps = Stream.ofAll(new TraversalHasStepFinder(step -> step.getHasContainers().get(0).getKey().equals(T.label.getAccessor()))
                    .getValue(newConstraint)).toJavaList();

            if (!labelHasSteps.isEmpty()) {
                labelHasSteps.get(0).getTraversal().removeStep(labelHasSteps.get(0));
            }

            Traversal elementConstraintsTraversal = elementConstraints.size() > 1 ?
                    __.or(Stream.ofAll(elementConstraints).map(GraphElementConstraint::getTraversalConstraint).toJavaArray(Traversal.class)) :
                    elementConstraints.get(0).getTraversalConstraint();

            newConstraint = Stream.ofAll(new TraversalHasStepFinder(step -> true).getValue(newConstraint)).isEmpty() ?
                    elementConstraintsTraversal :
                    __.and(elementConstraintsTraversal, newConstraint);

        }

        if (!(newConstraint.asAdmin().getSteps().get(0) instanceof AndStep)) {
            newConstraint = __.and(newConstraint);
        }

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query();//.filtered().filter();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(newConstraint);
        return true;
    }
    //endregion

    //region Private Methods
    private Set<String> getContextRelevantLabels(CompositeControllerContext context) {
        if (context.getVertexControllerContext().isPresent()) {
            return getVertexContextRelevantLabels(context);
        }

        return getElementContextRelevantLabels(context);
    }

    private Set<String> getElementContextRelevantLabels(ElementControllerContext context) {
        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        return labels;
    }

    private Set<String> getVertexContextRelevantLabels(VertexControllerContext context) {
        // currently assuming homogeneous bulk
        return Stream.ofAll(context.getBulkVertices())
                .take(1)
                .map(Element::label)
                .toJavaSet();
    }
    //endregion
}
