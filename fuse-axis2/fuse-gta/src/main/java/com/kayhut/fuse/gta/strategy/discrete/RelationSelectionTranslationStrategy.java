package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.properties.RedundantSelectionRelProp;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;

import java.util.Optional;

/**
 * Created by roman.margolis on 14/11/2017.
 */
public class RelationSelectionTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationSelectionTranslationStrategy(Class<? extends PlanOpBase> klasses) {
        super(klasses);
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        Optional<RelationOp> lastRelationOp = RelationOp.class.equals(planOp.getClass()) ?
                Optional.of((RelationOp) planOp) :
                PlanUtil.prev(plan, planOp, RelationOp.class);

        if (!lastRelationOp.isPresent()) {
            return traversal;
        }

        Optional<RelationFilterOp> relationFilterOp = PlanUtil.next(plan, lastRelationOp.get(), RelationFilterOp.class);
        Optional<PlanOpBase> adjacentToRelationOp = PlanUtil.adjacentNext(plan, lastRelationOp.get());

        Stream.ofAll(TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class))
                .filter(hasStep -> isSelectionHasStep((HasStep<?>) hasStep))
                .forEach(step -> traversal.asAdmin().removeStep(step));

        Stream.ofAll(lastRelationOp.get().getAsgEBase().geteBase().getReportProps())
                .forEach(relProp -> traversal.has(context.getOnt().$property$(relProp).getName(),
                        SelectP.raw(context.getOnt().$property$(relProp).getName())));

        if (adjacentToRelationOp.isPresent() &&
                relationFilterOp.isPresent() &&
                adjacentToRelationOp.get().equals(relationFilterOp.get())) {
            Stream.ofAll(relationFilterOp.get().getAsgEBase().geteBase().getProps())
                    .filter(relProp -> RedundantSelectionRelProp.class.isAssignableFrom(relProp.getClass()))
                    .map(relProp -> (RedundantSelectionRelProp)relProp)
                    .forEach(relProp -> traversal.has(relProp.getRedundantPropName(),
                            SelectP.raw(relProp.getRedundantPropName())));
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private boolean isSelectionHasStep(HasStep<?> hasStep) {
        return !Stream.ofAll(hasStep.getHasContainers())
                .filter(hasContainer -> hasContainer.getBiPredicate() instanceof SelectP)
                .isEmpty();
    }
    //endregion
}