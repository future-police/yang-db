package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.composite.Plan.toPattern;

/**
 * Created by Roman on 25/04/2017.
 */
public class AdjacentPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            if (!(compositePlanOp.getOps().get(0) instanceof EntityOp)) {
                return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">");
            } else
                return ValidationContext.OK;
        }

        PlanOp currentPlanOp = compositePlanOp.getOps().get(opIndex);
        PlanOp previousPlanOp = compositePlanOp.getOps().get(opIndex - 1);

        if (currentPlanOp instanceof EntityFilterOp) {
            if (!(previousPlanOp instanceof EntityOp)) {
                return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">");
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query,
                    ((EntityFilterOp) currentPlanOp).getAsgEbase().geteNum(),
                    ((EntityOp) previousPlanOp).getAsgEbase().geteNum());
            return areFilterAndEntityAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof RelationFilterOp) {
            if (!(previousPlanOp instanceof RelationOp)) {
                return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp) +"<"+opIndex+">");

            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query,
                    ((RelationFilterOp) currentPlanOp).getAsgEbase().geteNum(),
                    ((RelationOp) previousPlanOp).getAsgEbase().geteNum());
            return areFilterAndRelationAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof GoToEntityOp) {
            return ValidationContext.OK;
        }

        if (currentPlanOp instanceof EntityOp) {
            Optional<RelationOp> previousRelationOp = getPreviousOp(compositePlanOp, opIndex, RelationOp.class);
            if (!previousRelationOp.isPresent()) {
                return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">");
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query,
                    ((EntityOp) currentPlanOp).getAsgEbase().geteNum(),
                    previousRelationOp.get().getAsgEbase().geteNum());
            return areEntityAndRelationAdjacent(path, compositePlanOp);
        }

        if (currentPlanOp instanceof RelationOp) {
            Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
            if (!previousEntityOp.isPresent()) {
                return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp)+"<"+opIndex+">");
            }

            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query,
                    ((RelationOp) currentPlanOp).getAsgEbase().geteNum(),
                    previousEntityOp.get().getAsgEbase().geteNum());
            return areEntityAndRelationAdjacent(path, compositePlanOp);
        }

        return ValidationContext.OK;
    }
    //endregion

    //region Private Methods
    private ValidationContext areFilterAndEntityAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOp compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                EPropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp));
        }
        return ValidationContext.OK;

    }

    private ValidationContext areFilterAndRelationAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOp compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> Rel.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                RelPropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp));

        }
        return ValidationContext.OK;
    }

    private ValidationContext areEntityAndRelationAdjacent(List<AsgEBase<? extends EBase>> path, CompositePlanOp compositePlanOp) {
        boolean b = Stream.ofAll(path).count(asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                Rel.class.isAssignableFrom(asgEBase.geteBase().getClass())) == 2;
        if (!b) {
            return new ValidationContext(false,"Adjacent:Validation failed on:" + toPattern(compositePlanOp));

        }
        return ValidationContext.OK;
    }

    private <T extends PlanOp> Optional<T> getPreviousOp(CompositePlanOp compositePlanOp, int opIndex, Class<?> klass) {
        while (opIndex > 0) {
            PlanOp planOp = compositePlanOp.getOps().get(--opIndex);
            if (klass.isAssignableFrom(planOp.getClass())) {
                return Optional.of((T) planOp);
            }
        }

        return Optional.empty();
    }
    //endregion
}
