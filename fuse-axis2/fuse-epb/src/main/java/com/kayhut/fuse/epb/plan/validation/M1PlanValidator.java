package com.kayhut.fuse.epb.plan.validation;

import com.codahale.metrics.Slf4jReporter;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.opValidator.*;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

import java.util.Collections;

/**
 * Created by Roman on 04/05/2017.
 */
public class M1PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    public M1PlanValidator() {
        super(Mode.all);

        CompositePlanOpValidator compositePlanOpValidator =
                new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator());
        compositePlanOpValidator.with(new ChainedPlanOpValidator(compositePlanOpValidator));

        this.validators = Collections.singletonList(new ChainedPlanValidator(compositePlanOpValidator));
    }

    @Override
    @LoggerAnnotation(name = "isPlanValid", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public ValidationContext isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
}
