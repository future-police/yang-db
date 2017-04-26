package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liorp on 4/26/2017.
 */
public interface PlanMockUtils {

    String EDGE_FILTER_STATISTICS = "edgeFilterStatistics";
    String EDGE_STATISTICS = "edgeStatistics";
    String NODE_FILTER_STATISTICS = "nodeFilterStatistics";
    String NODE_STATISTICS = "nodeStatistics";

    enum Type {
        CONCRETE(EConcrete.class, true),
        TYPED(ETyped.class, true),
        UN_TYPED(EUntyped.class, false);

        private Class<? extends EEntityBase> type;
        private boolean eTyped;

        Type(Class<? extends EEntityBase> type, boolean eTyped) {
            this.type = type;
            this.eTyped = eTyped;
        }

        public Class<? extends EEntityBase> getType() {
            return type;
        }

        public boolean iseTyped() {
            return eTyped;
        }
    }

    class PlanMockBuilder {
        private Map<Integer, Double> nodeStatistics;
        private Map<Integer, Double> nodeFilterStatistics;
        private Map<Integer, Double> edgeStatistics;
        private Map<Integer, Double> edgeFilterStatistics;
        private Map<PlanOpBase, Double> costs;

        private Plan plan;
        private Plan oldPlan;


        private PlanMockBuilder() {
            //plan
            plan = new Plan();
            //statistics
            nodeStatistics = new HashMap<>();
            nodeFilterStatistics = new HashMap<>();
            edgeStatistics = new HashMap<>();
            edgeFilterStatistics = new HashMap<>();
            costs = new HashMap<>();
        }

        public static PlanMockBuilder mock() {
            return new PlanMockBuilder();
        }

        public PlanMockBuilder entity(Type type, long total, int eType) throws Exception {
            EntityOp entityOp = new EntityOp();
            EEntityBase instance = type.type.newInstance();
            //no type => max nodes return
            nodeStatistics.put(eType, Double.MAX_VALUE);
            if (eType > 0) {
                ((Typed) instance).seteType(eType);
                nodeStatistics.put(eType, (double) total);
            }
            entityOp.setEntity(new AsgEBase<>(instance));
            plan = plan.withOp(entityOp);
            //statistics simulator
            costs.put(entityOp, (double) total);
            return this;
        }

        public PlanMockBuilder rel(Direction direction, int relType, long total) throws Exception {
            Rel rel = new Rel();
            rel.setDir(Direction.valueOf(direction.name()).to());
            rel.setrType(relType);
            RelationOp relationOp = new RelationOp(new AsgEBase<>(rel));
            //no type => max nodes return
            if (relType > 0) {
                edgeStatistics.put(relType, (double) total);
            }

            plan = plan.withOp(relationOp);
            //statistics simulator
            costs.put(relationOp, (double) total);
            return this;
        }

        public PlanMockBuilder entityFilter(double factor, int id) throws Exception {
            EProp eProp = new EProp();
            eProp.setpType(String.valueOf(id));
            EntityFilterOp filterOp = new EntityFilterOp(new AsgEBase<>(eProp));

            plan = plan.withOp(filterOp);
            nodeFilterStatistics.put(id,factor);
            //statistics simulator
            costs.put(filterOp, factor);
            return this;
        }

        public PlanMockBuilder relFilter(double factor, int id) throws Exception {
            RelProp relProp = new RelProp();
            relProp.setpType(String.valueOf(id));
            RelationFilterOp relationFilterOp = new RelationFilterOp(new AsgEBase<>(relProp));

            plan = plan.withOp(relationFilterOp);
            edgeFilterStatistics.put(id,factor);
            //statistics simulator
            costs.put(relationFilterOp, factor);
            return this;
        }

        public Plan plan() {
            return plan;
        }


        public PlanWithCost<Plan, PlanDetailedCost> planWithCost(long globalCost, long total) {
            Cost cost = new Cost(globalCost, total);
            List<PlanOpWithCost<Cost>> collect = oldPlan.getOps().stream().map(element -> new PlanOpWithCost<>(getCost(element), 1, element)).collect(Collectors.toList());
            return new PlanWithCost<>(oldPlan, new PlanDetailedCost(cost, collect));
        }

        private Cost getCost(PlanOpBase opBase) {
            return new Cost(costs.getOrDefault(opBase, 1d), costs.getOrDefault(opBase, 1d).longValue());
        }

        public Map<PlanOpBase, Double> costs() {
            return costs;
        }

        public Map<String, Map<Integer,Double>> statistics() {
            Map<String, Map<Integer,Double>> map = new HashMap<>();
            map.put(EDGE_FILTER_STATISTICS,edgeFilterStatistics);
            map.put(EDGE_STATISTICS,edgeStatistics);
            map.put(NODE_FILTER_STATISTICS,nodeFilterStatistics);
            map.put(NODE_STATISTICS,nodeStatistics);
            return map;
        }

        public PlanMockBuilder startNewPlan() {
            oldPlan = plan;
            return this;
        }
    }
}
