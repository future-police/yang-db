package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.execution.plan.IPlan;

/**
 * Created by moti on 2/23/2017.
 */
public class NoPruningPruneStrategy<P extends IPlan> implements PlanPruneStrategy<P>{
    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        return plans;
    }
}
