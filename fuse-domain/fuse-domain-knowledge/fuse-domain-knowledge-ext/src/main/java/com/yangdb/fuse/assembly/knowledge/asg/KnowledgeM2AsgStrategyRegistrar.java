package com.yangdb.fuse.assembly.knowledge.asg;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.asg.strategy.AsgNamedParametersStrategy;
import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.asg.strategy.AsgStrategyRegistrar;
import com.yangdb.fuse.asg.strategy.RuleBoostProvider;
import com.yangdb.fuse.asg.strategy.constraint.*;
import com.yangdb.fuse.asg.strategy.propertyGrouping.*;
import com.yangdb.fuse.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import com.yangdb.fuse.asg.strategy.selection.DefaultETagAsgStrategy;
import com.yangdb.fuse.asg.strategy.selection.DefaultRelationSelectionAsgStrategy;
import com.yangdb.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.yangdb.fuse.asg.strategy.type.RelationPatternRangeAsgStrategy;
import com.yangdb.fuse.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import com.yangdb.fuse.assembly.knowledge.KnowledgeRuleBoostProvider;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.query.entity.EEntityBase;

import java.util.Arrays;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeRoutedSchemaProviderFactory.SchemaFields.*;
import static com.yangdb.fuse.assembly.knowledge.consts.physicalElementProperties.PhysicalReferenceProperties.CONTENT;

public class KnowledgeM2AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public KnowledgeM2AsgStrategyRegistrar(OntologyProvider ontologyProvider,
                                           GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.ruleBoostProvider = new KnowledgeRuleBoostProvider();
    }

    public KnowledgeM2AsgStrategyRegistrar(OntologyProvider ontologyProvider,
                                           GraphElementSchemaProviderFactory schemaProviderFactory,
                                           RuleBoostProvider ruleBoostProvider) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.ruleBoostProvider = ruleBoostProvider;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DefaultETagAsgStrategy(this.ontologyProvider),
                new KnowledgeLogicalEntityGraphTranslatorStrategy(this.schemaProviderFactory,this.ontologyProvider, EEntityBase.class),
                new AsgNamedParametersStrategy(),
                new RelationPatternRangeAsgStrategy(),
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
                new ConstraintExpLowercaseTransformationAsgStrategy(Arrays.asList(STRING_VALUE,CONTENT,TITLE,DISPLAY_NAME,DESCRIPTION)),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                //knowledge ranking asg strategies
                new KnowledgeLikeCombinerStrategy(ruleBoostProvider, ontologyProvider, schemaProviderFactory),
                new ConstraintExpCharEscapeTransformationAsgStrategy(),
                new RankingPropertiesPropagationAsgStrategy(),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider),
                new DefaultRelationSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private RuleBoostProvider ruleBoostProvider;
    //endregion
}
