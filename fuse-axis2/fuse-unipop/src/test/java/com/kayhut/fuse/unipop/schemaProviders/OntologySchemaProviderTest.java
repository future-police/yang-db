package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProviderTest {

    //region Tests
    @Test
    public void getVertexSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphVertexSchema vertexPersonSchema = ontologySchemaProvider.getVertexSchema("Person").get();

        assertEquals(vertexPersonSchema.getType(), "Person");
        assertEquals(1, Stream.ofAll(vertexPersonSchema.getIndexPartitions()).size());
        assertEquals(2, Stream.ofAll(Stream.ofAll(vertexPersonSchema.getIndexPartitions()).get(0).getIndices()).size());

        Iterable<String> indices = Stream.ofAll(Stream.ofAll(vertexPersonSchema.getIndexPartitions()).get(0).getIndices());

        assertEquals("vertexIndex1", Stream.ofAll(indices).get(0));
        assertEquals("vertexIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchema() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        GraphEdgeSchema edgeDragonFiresPersonSchema = ontologySchemaProvider.getEdgeSchema("Fire", Optional.of("Dragon"), Optional.of("Person")).get();
        assertEquals(edgeDragonFiresPersonSchema.getDestination().get().getType().get(), "Person");

        assertEquals(1, Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartitions()).size());
        assertEquals(2, Stream.ofAll(Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartitions()).get(0).getIndices()).size());

        Iterable<String> indices = Stream.ofAll(Stream.ofAll(edgeDragonFiresPersonSchema.getIndexPartitions()).get(0).getIndices());

        assertEquals("edgeIndex1", Stream.ofAll(indices).get(0));
        assertEquals("edgeIndex2", Stream.ofAll(indices).get(1));
    }

    @Test
    public void getEdgeSchemas() throws Exception {
        Ontology ontology = getOntology();
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(ontology);
        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = ontologySchemaProvider.getEdgeSchemas("Fire");
        assertEquals(Lists.newArrayList(edgeSchemas.get()).size(), 1);
    }
    //ednregion

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider((label, elementType) -> {
            if (elementType == PhysicalIndexProvider.ElementType.vertex) {
                return Collections.singletonList(() -> Arrays.asList("vertexIndex1", "vertexIndex2"));
            } else if (elementType == PhysicalIndexProvider.ElementType.edge) {
                return Collections.singletonList(() -> Arrays.asList("edgeIndex1", "edgeIndex2"));
            } else {
                // must fail
                Assert.assertTrue(false);
                return null;
            }
        }, ontology);
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA(2);
            seteTypeB(1);
        }});
        RelationshipType fireRelationshipType = RelationshipType.RelationshipTypeBuilder.aRelationshipType()
                .withRType(1).withName("Fire").withEPairs(ePairs).build();
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Dragon").build());
                    return entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return relTypes;
                }
        );

        return ontology;
    }
    //endregion
}