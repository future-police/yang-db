package com.kayhut.fuse.epb.plan.modules;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M2PatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M2.M2PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.M2GlobalPruner;
import com.kayhut.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.kayhut.fuse.epb.plan.statistics.ElasticCountStatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.validation.M2PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

/**
 *
 */
public class EpbModuleM2CountBased extends BaseEpbModule {
    //region ModuleBase Implementation


    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config config) {
        return M2PlanExtensionStrategy.class;
    }


    protected Class<? extends PlanValidator<Plan, AsgQuery>> planValidator() {
        return M2PlanValidator.class;
    }

    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {

                this.bind(StatisticsProviderFactory.class).to(ElasticCountStatisticsProviderFactory.class).asEagerSingleton();

                this.bind(CostEstimationConfig.class)
                        .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
                this.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {
                }).to(M2PatternCostEstimator.class).asEagerSingleton();

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .to(RegexPatternCostEstimator.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(RegexPatternCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});

                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
            }
        });
    }

    @Override
    protected PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPrunerStrategy(Config config) {
        return new M2GlobalPruner();
    }

    @Override
    protected PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector(Config config) {
        return new CheapestPlanSelector();
    }
    //endregion
}