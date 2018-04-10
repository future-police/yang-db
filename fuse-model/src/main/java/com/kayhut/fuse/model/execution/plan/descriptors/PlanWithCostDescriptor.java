package com.kayhut.fuse.model.execution.plan.descriptors;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.inject.Inject;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.CompositeAsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.query.EBase;

import java.util.*;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanWithCostDescriptor<P, C> implements Descriptor<PlanWithCost<P, C>> {
    //region Constructors
    @Inject
    public PlanWithCostDescriptor(Descriptor<? super P> planDescriptor, Descriptor<? super C> costDescriptor) {
        this.planDescriptor = planDescriptor;
        this.costDescriptor = costDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(PlanWithCost<P, C> planWithCost) {
        return "{" +
                " plan:" + this.planDescriptor.describe(planWithCost.getPlan()) + "," + "\n" +
                " cost:" + this.costDescriptor.describe(planWithCost.getCost()) + "\n" +
                "}";
    }
    //endregion

    public static Graph<GraphElement> graph(PlanWithCost<Plan, PlanDetailedCost> planWithCost,boolean cycle) {

        MutableGraph<GraphElement> build = GraphBuilder.directed().allowsSelfLoops(cycle).build();
        List<PlanOp> elements = planWithCost.getPlan().getOps();
        if (elements.isEmpty())
            return build;

        build.addNode(new GraphElement(((AsgEBaseContainer) elements.get(0))));

        for (int i = 1; i < elements.size(); i++) {
            //region optional
            if (elements.get(i) instanceof OptionalOp) {
                //add optional branch to graph
                List<PlanOp> ops = ((OptionalOp) elements.get(i)).getOps();
                build.addNode(new GraphElement(((AsgEBaseContainer) ops.get(0))));

                //continue optional sib branch
                for (int j = 1; j < ops.size(); j++) {
                    build.addNode(new GraphElement(((AsgEBaseContainer) ops.get(j))));
                    build.putEdge(new GraphElement(((AsgEBaseContainer) ops.get(j - 1))),
                                  new GraphElement(((AsgEBaseContainer) ops.get(j))));
                }
            } else {
                //endregion
                build.addNode(new GraphElement(((AsgEBaseContainer) elements.get(i))));
                if (cycle && elements.get(i - 1) instanceof OptionalOp) {
                    OptionalOp planOp = (OptionalOp) elements.get(i - 1);
                    AsgEBaseContainer lastOp = (AsgEBaseContainer) planOp.getOps().get(planOp.getOps().size() - 1);
                    build.putEdge(new GraphElement(lastOp), new GraphElement(((AsgEBaseContainer) elements.get(i))));
                } else {
                    if(!cycle && elements.get(i) instanceof GoToEntityOp) {
                       continue;
                    }
                    build.putEdge(new GraphElement(((AsgEBaseContainer) elements.get(i - 1))),
                                  new GraphElement(((AsgEBaseContainer) elements.get(i))));
                }
            }
        }
        return build;
    }

    public static String print(PlanWithCost<Plan, PlanDetailedCost> planWithCost) {
        List<String> builder = new LinkedList<>();
        builder.add("cost:" + planWithCost.getCost().getGlobalCost() + "\n");
        print(new HashMap<>(), builder, planWithCost.getPlan().getOps(), 1);
        return builder.toString();
    }


    static <T extends EBase> String print(Map<Integer, Integer> cursorLocations, List<String> builder, List<PlanOp> ops, int level) {
        builder.add("");
        int currentLine = 0;
        for (PlanOp currentOp : ops) {
            if (currentOp instanceof CompositeAsgEBasePlanOp) {
                builder.add(print(cursorLocations, new ArrayList<>(), ((CompositeAsgEBasePlanOp<T>) currentOp).getOps(), 0));
            } else if (currentOp instanceof AsgEBasePlanOp) {
                AsgEBasePlanOp<T> planOp = (AsgEBasePlanOp<T>) currentOp;
                String text = QueryDescriptor.getPrefix(isTail(planOp), planOp.getAsgEbase().geteBase()) + shortLabel(planOp, new StringJoiner(":"));
                if (planOp instanceof GoToEntityOp) {
                    char[] zeros = new char[cursorLocations.get(((GoToEntityOp) planOp).getAsgEbase().geteNum()) - 5];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                } else if (planOp instanceof EntityJoinOp) {
                    char[] zeros = new char[cursorLocations.getOrDefault(((EntityJoinOp) planOp).getAsgEbase().geteNum(), 0)];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                    builder.add("\n\t\t" + print(cursorLocations, new ArrayList<>(), ((EntityJoinOp) planOp).getLeftBranch().getOps(), 0));
                    level++;
                    builder.add("\n\t\t" + print(cursorLocations, new ArrayList<>(), ((EntityJoinOp) planOp).getRightBranch().getOps(), 0));
                    level++;
                } else if (planOp instanceof EntityNoOp) {
                    char[] zeros = new char[cursorLocations.get(((EntityNoOp) planOp).getAsgEbase().geteNum()) - 5];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                } else {
                    builder.set(level + currentLine, builder.get(level + currentLine) + text);
                    cursorLocations.put(planOp.getAsgEbase().geteNum(), builder.get(level + currentLine).length() - 1);
                }
            }
        }
        return builder.toString();
    }

    private static <T extends EBase> boolean isTail(AsgEBasePlanOp<T> planOp) {
        return planOp instanceof EntityNoOp || planOp instanceof GoToEntityOp || planOp instanceof EntityJoinOp;
    }

    private static <T extends EBase> String shortLabel(AsgEBasePlanOp<T> element, StringJoiner joiner) {
        if (element instanceof GoToEntityOp) {
            return joiner.add("goTo[" + element.getAsgEbase().geteNum() + "]").toString();
        }
        if (element instanceof EntityJoinOp) {
            AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner);
            joiner.add("join[" + element.getAsgEbase().geteNum() + "]");
            return joiner.toString();
        }
        if (element instanceof EntityNoOp) {
            return joiner.add("Opt[" + element.getAsgEbase().geteNum() + "]").toString();
        } else
            return AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner);
    }

    public static class GraphElement {
        private AsgEBaseContainer planOp;

        public GraphElement(AsgEBaseContainer planOp) {
            this.planOp = planOp;
        }

        public AsgEBaseContainer getPlanOp() {
            return planOp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphElement that = (GraphElement) o;

            return planOp != null ? planOp.getAsgEbase().equals(that.planOp.getAsgEbase()) : that.planOp == null;
        }

        @Override
        public int hashCode() {
            return planOp != null ? planOp.getAsgEbase().hashCode() : 0;
        }
    }

    //region Fields
    private Descriptor<? super P> planDescriptor;
    private Descriptor<? super C> costDescriptor;
    //endregion
}