package com.kayhut.fuse.dispatcher.epb;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.validation.QueryValidation;

import java.util.Optional;

/**
 * Created by Roman on 12/16/2017.
 */
public class PlanTracer {

    //region Builder
    public static class Builder {
        //region Constructors
        public Builder() {

        }
        //endregion
    }
    //endregion

    //region Searcher
    public static class Searcher<P, C, Q> implements PlanSearcher<P, C, Q> {
        public static final String injectionName = "PlanTracer.Searcher.@planSearcher";

        //region Constructors
        @Inject
        public Searcher(
                @Named(injectionName) PlanSearcher<P, C, Q> planSearcher,
                PlanTracer.Builder builder) {
            this.planSearcher = planSearcher;
            this.builder = builder;
        }
        //endregion

        //region PlanSearcher Implementation
        @Override
        public PlanWithCost<P, C> search(Q query) {
            return this.planSearcher.search(query);
        }
        //endregion

        //region Fields
        private PlanSearcher<P, C, Q> planSearcher;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P, C, Q> implements com.google.inject.Provider<PlanSearcher<P, C, Q>> {
            public static final String injectionName = "PlanTracer.Searcher.Provider.@planSearcher";

            //region Constructors
            @Inject
            public Provider(
                    @Named(injectionName) PlanSearcher<P, C, Q> planSearcher,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planSearcher = planSearcher;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanSearcher<P, C, Q> get() {
                return isVerbose ?
                        new Searcher<>(this.planSearcher, this.builder) :
                        this.planSearcher;
            }
            //endregion

            //region Fields
            private PlanSearcher<P, C, Q> planSearcher;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region ExtensionStrategy
    public static class ExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P, Q> {
        public static final String injectionName = "PlanTracer.ExtensionStrategy.@planExtensionStrategy";

        //region Constructors
        @Inject
        public ExtensionStrategy(@Named(injectionName) PlanExtensionStrategy<P, Q> planExtensionStrategy, PlanTracer.Builder builder) {
            this.planExtensionStrategy = planExtensionStrategy;
            this.builder = builder;
        }
        //endregion

        //region PlanExtensionStrategy Implementation
        @Override
        public Iterable<P> extendPlan(Optional<P> plan, Q query) {
            return this.planExtensionStrategy.extendPlan(plan, query);
        }
        //endregion

        //region Fields
        private PlanExtensionStrategy<P, Q> planExtensionStrategy;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, Q extends IQuery> implements com.google.inject.Provider<PlanExtensionStrategy<P, Q>> {
            public static final String injectionName = "PlanTracer.ExtensionStrategy.Provider.@planExtensionStrategy";

            //region Constructors
            @Inject
            public Provider(
                    @Named(injectionName) PlanExtensionStrategy<P, Q> planExtensionStrategy,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planExtensionStrategy = planExtensionStrategy;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanExtensionStrategy<P, Q> get() {
                return isVerbose ?
                        new ExtensionStrategy<>(this.planExtensionStrategy, this.builder) :
                        this.planExtensionStrategy;
            }
            //endregion

            //region Fields
            private PlanExtensionStrategy<P, Q> planExtensionStrategy;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Validator
    public static class Validator<P, Q> implements PlanValidator<P, Q> {
        public static final String injectionName = "PlanTracer.Validator.@planValidator";

        //region Constructors
        @Inject
        public Validator(
                @Named(injectionName) PlanValidator<P, Q> planValidator,
                PlanTracer.Builder builder) {
            this.planValidator = planValidator;
            this.builder = builder;
        }
        //endregion

        //region PlanValidator Implementation
        @Override
        public QueryValidation isPlanValid(P plan, Q query) {
            return this.planValidator.isPlanValid(plan, query);
        }
        //endregion

        //region Fields
        private PlanValidator<P, Q> planValidator;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, Q extends IQuery> implements com.google.inject.Provider<PlanValidator<P, Q>> {
            public static final String injectionName = "PlanTracer.Validator.Provider.@planValidator";

            //region Constructors
            @Inject
            public Provider(
                    @Named(injectionName) PlanValidator<P, Q> planValidator,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planValidator = planValidator;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanValidator<P, Q> get() {
                return isVerbose ?
                        new Validator<>(this.planValidator, this.builder) :
                        this.planValidator;
            }
            //endregion

            //region Fields
            private PlanValidator<P, Q> planValidator;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Estimator
    public static class Estimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
        public static final String injectionName = "PlanTracer.Estimator.@costEstimator";

        //region Constructors
        @Inject
        public Estimator(
                @Named(injectionName) CostEstimator<P, C, TContext> costEstimator,
                PlanTracer.Builder builder) {
            this.costEstimator = costEstimator;
            this.builder = builder;
        }
        //endregion

        //region CostEstimator Implementation
        @Override
        public PlanWithCost<P, C> estimate(P plan, TContext context) {
            return this.costEstimator.estimate(plan, context);
        }
        //endregion

        //region Fields
        private CostEstimator<P, C, TContext> costEstimator;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, C extends Cost, TContext> implements com.google.inject.Provider<CostEstimator<P, C, TContext>> {
            public static final String injectionName = "PlanTracer.Estimator.Provider.@costEstimator";

            //region Constructors
            @Inject
            public Provider(
                    @Named(injectionName) CostEstimator<P, C, TContext> costEstimator,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.costEstimator = costEstimator;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public CostEstimator<P, C, TContext> get() {
                return isVerbose ?
                        new Estimator<>(this.costEstimator, this.builder) :
                        this.costEstimator;
            }
            //endregion

            //region Fields
            private CostEstimator<P, C, TContext> costEstimator;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion
}
