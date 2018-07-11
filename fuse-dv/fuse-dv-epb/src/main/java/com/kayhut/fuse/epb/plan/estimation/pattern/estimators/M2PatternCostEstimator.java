package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.*;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M2PatternCostEstimator extends CompositePatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Pattern>,
            PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider,
                    RegexPatternCostEstimator regexPatternCostEstimator) {
        Map<Class<? extends Pattern>, PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityPattern.class, new EntityPatternCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityPattern.class, new EntityRelationEntityPatternCostEstimator(config, statisticsProviderFactory, ontologyProvider));
        estimators.put(GoToEntityRelationEntityPattern.class,
                new GoToEntityRelationEntityPatternCostEstimator(
                        (EntityRelationEntityPatternCostEstimator)estimators.get(EntityRelationEntityPattern.class)));
        EntityJoinPatternCostEstimator entityJoinPatternCostEstimator = new EntityJoinPatternCostEstimator();
        entityJoinPatternCostEstimator.setCostEstimator(regexPatternCostEstimator);
        estimators.put(EntityJoinPattern.class, entityJoinPatternCostEstimator);
        estimators.put(EntityJoinEntityPattern.class, new EntityJoinRelationEntityPatternCostEstimator((EntityRelationEntityPatternCostEstimator)estimators.get(EntityRelationEntityPattern.class)));
        estimators.put(GotoPattern.class, new GotoPatternCostEstimator());

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M2PatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider,
            RegexPatternCostEstimator regexPatternCostEstimator) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider, regexPatternCostEstimator));
    }
    //endregion
}