package com.kayhut.fuse.epb.plan.estimation.pattern.estimators.rule;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityRelationEntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.GoToEntityRelationEntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.*;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

public class RulesBasedPatternCostEstimator extends CompositePatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Pattern>,
            PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider) {
        Map<Class<? extends Pattern>, PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityPattern.class, new EntityPatternCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityPattern.class, new EntityRelationEntityPatternCostEstimator(config, statisticsProviderFactory, ontologyProvider));

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public RulesBasedPatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider));
    }
    //endregion
}