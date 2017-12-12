package com.kayhut.fuse.dispatcher.epb;

import com.kayhut.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanSearcher<P, C, Q> {
     PlanWithCost<P, C> search(Q query);
}