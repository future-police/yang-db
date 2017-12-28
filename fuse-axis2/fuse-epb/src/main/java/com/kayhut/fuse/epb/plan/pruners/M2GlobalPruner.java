package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Arrays;
import java.util.List;

public class M2GlobalPruner extends CompositePruner<PlanWithCost<Plan, PlanDetailedCost>> {
    public M2GlobalPruner() {
        super(pruners());
    }

    private static List<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>> pruners() {
        return Arrays.asList(new SymmetricalJoinPruner());
    }
}