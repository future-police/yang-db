package com.kayhut.fuse.asg.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.constraint.*;
import com.kayhut.fuse.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.HQuantPropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.kayhut.fuse.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;

import java.util.Arrays;

/**
 * Created by roman.margolis on 07/02/2018.
 */
public class M2AsgStrategyRegistrar  implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M2AsgStrategyRegistrar(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
