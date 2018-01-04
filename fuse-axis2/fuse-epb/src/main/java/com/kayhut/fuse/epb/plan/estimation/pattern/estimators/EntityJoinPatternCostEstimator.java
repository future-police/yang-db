package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityJoinPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

import java.util.Optional;

/**
 * Estimates the cost of a join pattern.
 * There are two main cases:
 *  1. A new join op
 *  2. An existing join op that has been extended
 */
public class EntityJoinPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {

    //TODO: decompose method to two separate methods each handing a join scenario (new join, and ongoing join)
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        EntityJoinPattern entityJoinPattern = (EntityJoinPattern) pattern;
        PlanDetailedCost leftBranchCost = null;
        PlanDetailedCost rightBranchCost = null;
        boolean newJoin = true;

        // Check if our plan is an extension of an existing join
        if(context.getPreviousCost().get().getPlan().getOps().get(0) instanceof EntityJoinOp){
            EntityJoinOp previousJoinOp = (EntityJoinOp)context.getPreviousCost().get().getPlan().getOps().get(0);
            // If previous join and current join have same left branches - this is an extension
            if(previousJoinOp.getLeftBranch().equals(entityJoinPattern.getEntityJoinOp().getLeftBranch())){
                newJoin = false;
                JoinCost previousJoinCost = (JoinCost) context.getPreviousCost().get().getCost().getPlanStepCost(previousJoinOp).get().getCost();
                leftBranchCost = previousJoinCost.getLeftBranchCost();
                PlanWithCost<Plan, PlanDetailedCost> rightPlanWithCostOld = new PlanWithCost<>(previousJoinOp.getRightBranch(), previousJoinCost.getRightBranchCost());
                rightBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(),
                                                new IncrementalEstimationContext<>(Optional.of(rightPlanWithCostOld), context.getQuery())).getCost();
            }
        }

        if(newJoin) {
            if(context.getPreviousCost().get().getPlan().equals(entityJoinPattern.getEntityJoinOp().getLeftBranch())) {
                leftBranchCost = context.getPreviousCost().get().getCost();
            }else{
                leftBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getLeftBranch(), context).getCost();
            }
            rightBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(), new IncrementalEstimationContext<>(Optional.empty(), context.getQuery())).getCost();
        }

        return PatternCostEstimator.Result.of(1.0,
                new PlanWithCost<>(new Plan(entityJoinPattern.getEntityJoinOp()),
                        new JoinCost(calcJoinCost(leftBranchCost,rightBranchCost, entityJoinPattern.getEntityJoinOp()),
                                calcJoinCounts(leftBranchCost,rightBranchCost, entityJoinPattern.getEntityJoinOp()), leftBranchCost, rightBranchCost)));
    }

    private double calcJoinCost(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp joinOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class).get()).get();
        EntityOp rightBranchLastEntityOp = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class).get();
        if(rightBranchLastEntityOp.getAsgEbase().equals(joinOp.getAsgEbase()))
            return leftOpCost.getCost().peek() + rightCost.getPlanStepCost(rightBranchLastEntityOp).get().getCost().peek();
        else
            return 0;
    }

    private double calcJoinCounts(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp joinOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class).get()).get();
        EntityOp rightBranchLastEntityOp = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class).get();
        if(rightBranchLastEntityOp.getAsgEbase().equals(joinOp.getAsgEbase()))
            return Math.min(leftOpCost.getCost().peek() , rightCost.getPlanStepCost(rightBranchLastEntityOp).get().getCost().peek());
        else
            return 0;
    }

    public void setCostEstimator(CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator) {
        this.costEstimator = costEstimator;
    }

    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator;
}
