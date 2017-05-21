package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/27/2017.
 */
public class CompositePlanExtensionStrategy<P, Q> implements PlanExtensionStrategy<P, Q> {

    private PlanExtensionStrategy<P,Q>[] innerExtenders;

    @Inject
    @SafeVarargs
    public CompositePlanExtensionStrategy(PlanExtensionStrategy<P, Q> ... innerExtenders) {
        this.innerExtenders = innerExtenders;
    }

    @Override
    public Iterable<P> extendPlan(Optional<P> plan, Q query) {
        List<P> plans = new LinkedList<>();
        for(PlanExtensionStrategy<P,Q> extensionStrategy : innerExtenders){
            extensionStrategy.extendPlan(plan, query).forEach(plans::add);
        }
        return plans;
    }
}
