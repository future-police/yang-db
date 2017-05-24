package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.gta.strategy.utils.EntityTranslationUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase<EntityFilterOp> {
    //region Constructors
    public EntityFilterOpTranslationStrategy() {
        super(EntityFilterOp.class);
    }
    //endregion
    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, EntityFilterOp planOp, TranslationContext context) {
        Optional<PlanOpBase> previousPlanOp = PlanUtil.adjacentPrev(plan, planOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }

        if (HasStep.class.isAssignableFrom(traversal.asAdmin().getEndStep().getClass())) {
            traversal.asAdmin().removeStep(traversal.asAdmin().getSteps().indexOf(traversal.asAdmin().getEndStep()));
        }

        if (VertexStep.class.isAssignableFrom(traversal.asAdmin().getEndStep().getClass())) {
            VertexStep vertexStep = (VertexStep)traversal.asAdmin().getEndStep();
            if (vertexStep.getEdgeLabels() != null && vertexStep.getEdgeLabels().length == 1 &&
                    vertexStep.getEdgeLabels()[0].equals(GlobalConstants.Labels.PROMISE_FILTER)) {
                traversal.asAdmin().removeStep(traversal.asAdmin().getSteps().indexOf(traversal.asAdmin().getEndStep()));
            }
        }

        EntityOp entityOp = (EntityOp)previousPlanOp.get();
        if (PlanUtil.isFirst(plan, entityOp)) {
            traversal = appendEntityAndPropertyGroup(
                    traversal,
                    entityOp.getAsgEBase().geteBase(),
                    planOp.getAsgEBase().geteBase(),
                    context.getOnt());

        } else if (!planOp.getAsgEBase().geteBase().getProps().isEmpty()) {

            traversal = appendPropertyGroup(
                    traversal,
                    planOp.getAsgEBase().geteBase(),
                    context.getOnt());
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendEntityAndPropertyGroup(
            GraphTraversal traversal,
            EEntityBase entity,
            EPropGroup ePropGroup,
            Ontology.Accessor ont) {

        if (entity instanceof EConcrete) {
            //traversal.has(GlobalConstants.HasKeys.PROMISE, P.eq(Promise.as(((EConcrete) entity).geteID())));
            traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                    P.eq(Constraint.by(__.has(T.id, P.eq(((EConcrete)entity).geteID())))));
        }
        else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            Traversal constraintTraversal = __.has(T.label, P.eq(GlobalConstants.Labels.NONE));
            if (eTypeNames.size() == 1) {
                constraintTraversal = __.has(T.label, P.eq(eTypeNames.get(0)));
            } else if (eTypeNames.size() > 1) {
                constraintTraversal = __.has(T.label, P.within(eTypeNames));
            }

            List<Traversal> epropTraversals =
                    Stream.ofAll(ePropGroup.getProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ont)).toJavaList();

            if (!epropTraversals.isEmpty()) {
                epropTraversals.add(0, constraintTraversal);
                constraintTraversal = __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));
            }

            traversal.has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal));
        }

        return traversal;
    }

    private GraphTraversal appendPropertyGroup(
            GraphTraversal traversal,
            EPropGroup ePropGroup,
            Ontology.Accessor ont) {

        List<Traversal> epropTraversals =
                Stream.ofAll(ePropGroup.getProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ont)).toJavaList();

        Traversal constraintTraversal = epropTraversals.size() == 1 ?
                epropTraversals.get(0) :
                __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));

        return traversal.outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal))
                .otherV();
    }

    private Traversal convertEPropToTraversal(EProp eProp, Ontology.Accessor ont) {
         Optional<Property> property = ont.$property(Integer.parseInt(eProp.getpType()));
         if (!property.isPresent()) {
             return __.start();
         }

         return __.has(property.get().getName(), ConverstionUtil.convertConstraint(eProp.getCon()));
    }
    //endregion
}
