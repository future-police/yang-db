package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class RelationOpTranslationStrategyTest {
    @Test
    public void test_entity1_rel2_entity3() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(query, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(1).withName("Person").build(),
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(2).withName("Dragon").build()
                    );
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOp()).thenAnswer(invocationOnMock -> plan.getOps().get(1));

        RelationOpTranslationStrategy relationOpTranslationStrategy = new RelationOpTranslationStrategy();
        GraphTraversal actualTraversal = relationOpTranslationStrategy.apply(context, __.start());

        GraphTraversal expectedTraversal = __.start().outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_entity3_rel2_entity1() throws Exception {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 3).get()),
                new RelationOp(AsgQueryUtils.reverseRelation(AsgQueryUtils.<Rel>getElement(query, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(query, 1).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    return Arrays.asList(
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(1).withName("Person").build(),
                            EntityType.EntityTypeBuilder.anEntityType()
                                    .withEType(2).withName("Dragon").build()
                    );
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );

        TranslationStrategyContext context = Mockito.mock(TranslationStrategyContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(context.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(context.getPlanOp()).thenAnswer(invocationOnMock -> plan.getOps().get(1));

        RelationOpTranslationStrategy relationOpTranslationStrategy = new RelationOpTranslationStrategy();
        GraphTraversal actualTraversal = relationOpTranslationStrategy.apply(context, __.start());

        GraphTraversal expectedTraversal = __.start().outE(GlobalConstants.Labels.PROMISE).as("B-->A")
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.IN))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}