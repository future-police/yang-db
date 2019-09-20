package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GraphElementSchemaProviderJsonFactoryTest {
    private ObjectMapper mapper = new ObjectMapper();
    private IndexProvider provider;
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProvider.conf");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");
        ontology = mapper.readValue(stream, Ontology.class);
    }

    @Test
    public void testGraphElementSchemaProvider(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider,ontology);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
    }

    @Test
    public void testGraphElementSchemaProviderLabel(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider,ontology);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getEdgeLabels().spliterator(),false)
                .collect(Collectors.toSet()),new HashSet<>(Arrays.asList("Freeze","Fire","Own","SubjectOf","OriginatedIn","RegisteredIn","Know","MemberOf")));
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getVertexLabels().spliterator(),false)
                .collect(Collectors.toSet()),new HashSet<>(Arrays.asList("Horse","Guild","Person","Dragon","Kingdom")));
    }

    @Test
    public void testGraphElementSchemaProviderVertex(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider,ontology);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(5, StreamSupport.stream(schemaProvider.getVertexSchemas().spliterator(), false)
                .filter(p->p.getIndexPartitions().isPresent())
                .filter(p->p.getIndexPartitions().get() instanceof StaticIndexPartitions)
                .filter(p->p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
    }

    @Test
    public void testGraphEdgeSchemaImpl(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider,ontology);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Arrays.asList("Freeze","Fire","Own","SubjectOf","OriginatedIn","RegisteredIn","Know","MemberOf")
                .forEach(label->{
                    Assert.assertNotNull(schemaProvider.getEdgeSchemas(label));
                    GraphEdgeSchema schema = schemaProvider.getEdgeSchemas(label).iterator().next();
                    switch (schema.getLabel()) {
                        case "Freeze":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 2);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Freeze)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Fire":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 2);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Fire)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Own":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Own)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "SubjectOf":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(SubjectOf)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "OriginatedIn":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(OriginatedIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "RegisteredIn":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(RegisteredIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Know":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 2);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Know)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;

                        case "MemberOf":
                            Assert.assertEquals(schema.getDirection(), Direction.OUT);
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(MemberOf)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;

                        default:
                            Assert.assertTrue("No other Edge label should exist",false);
                    }
                });
    }

    @Test
    public void testGraphElementSchemaProviderEdge(){
        GraphElementSchemaProviderJsonFactory jsonFactory = new GraphElementSchemaProviderJsonFactory(provider,ontology);
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(12, StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false)
                .filter(p->p.getIndexPartitions().isPresent())
                .filter(p->p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
        schemaProvider.getEdgeSchemas().forEach(schema -> {
            switch (schema.getLabel()) {
                case "Freeze":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Freeze").spliterator().estimateSize(), 1);
                    break;
                case "Fire":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Fire").spliterator().estimateSize(), 1);
                    break;
                case "Own":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Own").spliterator().estimateSize(), 2);
                    break;
                case "SubjectOf":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("SubjectOf").spliterator().estimateSize(), 1);
                    break;
                case "OriginatedIn":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("OriginatedIn").spliterator().estimateSize(), 3);
                    break;
                case "RegisteredIn":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("RegisteredIn").spliterator().estimateSize(), 2);
                    break;
                case "Know":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Know").spliterator().estimateSize(), 1);
                    break;
                case "MemberOf":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("MemberOf").spliterator().estimateSize(), 1);
                    break;

                default:
                    Assert.assertTrue("No other Edge label should exist", false);
            }
        });
    }


    /**
     *                         new GraphEdgeSchema.Impl(
     *                                 "fire",
     *                                 new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList("entityA.id"),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     *                                         ))),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList("entityB.id"),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     *                                         ))),
     *                                 Direction.OUT,
     *                                 Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
     *                                 Optional.empty(),
     *                                 Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     *                                 Collections.emptyList(),
     *                                 Stream.of(endA).toJavaSet())
     */
}