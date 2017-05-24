package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 * Created by benishue on 12-Mar-17.
 */
public class TranslationContext {
    //region Constructors
    public TranslationContext(Ontology.Accessor ont, GraphTraversalSource graphTraversalSource) {
        this.ont = ont;
        this.graphTraversalSource = graphTraversalSource;
    }
    //endregion

    //region Properties
    public Ontology.Accessor getOnt() {
        return ont;
    }

    public GraphTraversalSource getGraphTraversalSource() {
        return this.graphTraversalSource;
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    private GraphTraversalSource graphTraversalSource;
    //endregion
}
