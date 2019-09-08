package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endB;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    public static final String ID = "id";
    public static final String ENTITY_A = "entityA";
    public static final String ENTITY_A_ID = "entityA.id";
    public static final String ENTITY_B = "entityB";
    public static final String ENTITY_B_ID = "entityB.id";
    public static final String DIRECTION = "direction";
    public static final String OUT = "out";
    public static final String IN = "in";

    public static final String STATIC = "static";
    public static final String TIME = "time";

    private IndexProvider indexProvider;
    private Ontology.Accessor accessor;

    @Inject
    public GraphElementSchemaProviderJsonFactory(IndexProvider indexProvider, Ontology ontology) {
        this.indexProvider = indexProvider;
        this.accessor = new Ontology.Accessor(ontology);
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new GraphElementSchemaProvider.Impl(
                getVertexSchemas(),
                getEdgeSchemas());
    }

    private List<GraphEdgeSchema> getEdgeSchemas() {
        return indexProvider.getRelations().stream()
                .flatMap(r -> generateGraphEdgeSchema(r).stream())
                .collect(Collectors.toList());
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r) {
        switch (r.getPartition()) {
            case STATIC:
                return generateGraphEdgeSchema(r, r.getType());
            case TIME:
                return generateGraphEdgeSchema(r, r.getType());
        }

        return Collections.singletonList(new GraphEdgeSchema.Impl(r.getType(),
                new StaticIndexPartitions(r.getProps().getValues().isEmpty() ? r.getType() : r.getProps().getValues().get(0))));
    }


    private List<GraphVertexSchema> getVertexSchemas() {
        return indexProvider.getEntities().stream().flatMap(e -> generateGraphVertexSchema(e).stream()).collect(Collectors.toList());
    }

    private List<GraphVertexSchema> generateGraphVertexSchema(Entity e) {
        switch (e.getPartition()) {
            case STATIC:
                return
                        e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(e.getType(), new StaticIndexPartitions(v)))
                                .collect(Collectors.toList());
            case TIME:
                e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(e.getType(), new TimeBasedIndexPartitions(e.getProps())))
                        .collect(Collectors.toList());
                break;
        }

        String v = e.getProps().getValues().isEmpty() ? e.getType() : e.getProps().getValues().get(0);
        return Collections.singletonList(new GraphVertexSchema.Impl(e.getType(), new StaticIndexPartitions(v)));
    }

    private Optional<List<EPair>> getEdgeSchemaOntologyPairs(String edge) {
        Optional<RelationshipType> relation = accessor.relation(edge);
        return relation.map(RelationshipType::getePairs);
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r, String v) {
        Optional<List<EPair>> pairs = getEdgeSchemaOntologyPairs(v);

        if (!pairs.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", "No edges pairs are found for given relation name " + v));

        List<EPair> pairList = pairs.get();
        validateSchema(pairList);

        return pairList.stream().map(p -> new GraphEdgeSchema.Impl(
                v,
                new GraphElementConstraint.Impl(__.has(T.label, v)),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList(ENTITY_A_ID),
                        Optional.of(p.geteTypeA()),
                        getGraphRedundantPropertySchemas(ENTITY_A, p.geteTypeA(), r))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList(ENTITY_B_ID),
                        Optional.of(p.geteTypeB()),
                        getGraphRedundantPropertySchemas(ENTITY_B, p.geteTypeB(), r))),
                Direction.OUT,
                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Collections.singletonList(v))),
                Collections.emptyList(),
                r.isSymmetric() ? Stream.of(endA, endB).toJavaSet() : Stream.of(endA).toJavaSet()))
                .collect(Collectors.toList());
    }

    private void validateSchema(List<EPair> pairList) {
        pairList.forEach(pair->{
                    if(!accessor.entity(pair.geteTypeA()).isPresent() ||
                        !accessor.entity(pair.geteTypeB()).isPresent())
                        throw new FuseError.FuseErrorException(new FuseError("Schema generation exception"," Pair containing "+pair.toString() + " was not matched against the current ontology"));
                });
    }

    private List<GraphRedundantPropertySchema> getGraphRedundantPropertySchemas(String entitySide,String entityType, Relation rel) {
        List<GraphRedundantPropertySchema> redundantPropertySchemas = new ArrayList<>();

        if(!accessor.entity(entityType).get().getProperties().contains(ID))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception"," Entity "+ entityType+"not containing "+ID + " property "));

        validateRedundant(entityType,entitySide,rel.getRedundant());
        redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(ID, String.format("%s.%s", entitySide, ID), "string"));
        //add all RedundantProperty according to schema
        validateRedundant(entityType,entitySide,rel.getRedundant());
        rel.getRedundant()
                .stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r -> {
                    redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(r.getName(), String.format("%s.%s", entitySide, r.getName()), r.getType()));
                });
        return redundantPropertySchemas;
    }

    private void validateRedundant(String entityType, String entitySide, List<Redundant> redundant) {
        redundant.stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r-> {
                        if(!accessor.entity(entityType).get().getProperties().contains(r.getName()))
                            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception"," Entity "+ entityType + " not containing "+r.getName() + " property (as redundant ) "  ));
                });
    }

    /**
     * new GraphEdgeSchema.Impl(
     * "fire",
     * new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList("entityA.id"),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     * new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     * ))),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList("entityB.id"),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     * new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     * ))),
     * Direction.OUT,
     * Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
     * Optional.empty(),
     * Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     * Collections.emptyList(),
     * Stream.of(endA).toJavaSet())
     */

    public static class TimeBasedIndexPartitions implements TimeSeriesIndexPartitions {
        private Props props;
        private SimpleDateFormat dateFormat;

        TimeBasedIndexPartitions(Props props) {
            this.props = props;
            this.dateFormat = new SimpleDateFormat(getDateFormat());
        }


        @Override
        public String getDateFormat() {
            return props.getDateFormat();
        }

        @Override
        public String getIndexPrefix() {
            return props.getPrefix();
        }

        @Override
        public String getIndexFormat() {
            return props.getIndexFormat();
        }

        @Override
        public String getTimeField() {
            return props.getPartitionField();
        }

        @Override
        public String getIndexName(Date date) {
            String format = String.format(getIndexFormat(), dateFormat.format(date));
            List<String> indices = Stream.ofAll(getPartitions())
                    .flatMap(Partition::getIndices)
                    .filter(index -> index.equals(format))
                    .toJavaList();

            return indices.isEmpty() ? null : indices.get(0);
        }

        @Override
        public Optional<String> getPartitionField() {
            return Optional.of(getTimeField());
        }

        @Override
        public Iterable<Partition> getPartitions() {
            return Collections.singletonList(() -> Stream.ofAll(props.getValues())
                    .map(p -> String.format(getIndexFormat(), p))
                    .distinct().sorted()
                    .toJavaList());
        }
    }


}
