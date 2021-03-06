package com.yangdb.fuse.assembly.knowledge.load.builder;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import com.yangdb.fuse.model.results.Relationship;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class RelationBuilder extends Metadata {

    public static final String type = "Relation";
    public static String physicalType = "relation";

    public String context = DEFAULT_CTX;
    public String category;
    public String techId;
    public String entityAId;
    public String entityBId;
    public String entityACategory;
    public String entityATechId;
    public String entityBCategory;
    public String entityBTechId;
    private String relId;
    public Map<String, Object> additionalProperties = new HashMap<>();
    public List<String> refs = new ArrayList<>();

    public List<Entity> subEntities = new ArrayList<>();
    public List<Relationship> hasValues = new ArrayList<>();
    public List<Relationship> hasRefs = new ArrayList<>();


    public RelationBuilder(RelationBuilder builder) {
        super(builder);
        this.relId = builder.relId;
        this.category = builder.category;
        this.techId = builder.techId;
        this.context = builder.context;
        this.entityAId = builder.entityAId;
        this.entityBId = builder.entityBId;
        this.entityACategory = builder.entityACategory;
        this.entityBCategory = builder.entityBCategory;
        this.entityATechId = builder.entityATechId;
        this.entityBTechId = builder.entityBTechId;
        this.refs = Arrays.asList(builder.refs.toArray(new String[builder.refs.size()]));
    }

    public RelationBuilder() {
    }

    public RelationBuilder putProperty(String key, Object value) {
        super.putProperty(key, value);
        switch (key) {
            case "category":
                return cat(value.toString());
            case "context":
                return ctx(value.toString());
            case "techId":
                return techId(value.toString());
            default:
                // suppose a property is set on the metadata (supper) entity it will also be added to
                // the additional properties but the serialization to jsonObject is map based so duplicates
                // will be eliminated
                additionalProperties.put(key, value);
                return this;
        }
    }

    public static RelationBuilder _rel(String id) {
        final RelationBuilder builder = new RelationBuilder();
        builder.relId = id;
        return builder;
    }

    public RelationBuilder sideA(EntityBuilder sideA) {
        this.entityAId = sideA.id();
        this.entityACategory = sideA.category;
        this.entityATechId = sideA.techId;
        return this;
    }

    public RelationBuilder sideB(EntityBuilder sideB) {
        this.entityBId = sideB.id();
        this.entityBCategory = sideB.category;
        this.entityBTechId = sideB.techId;
        return this;
    }

    public RelationBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public RelationBuilder techId(String techId) {
        this.techId = techId;
        return this;
    }

    public RelationBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    public RelationBuilder entityAId(String entityAId) {
        this.entityAId = entityAId;
        return this;
    }

    public RelationBuilder entityBId(String entityBId) {
        this.entityBId = entityBId;
        return this;
    }

    public RelationBuilder entityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
        return this;
    }

    public RelationBuilder entityATechId(String entityATechId) {
        this.entityATechId = entityATechId;
        return this;
    }

    public RelationBuilder entityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
        return this;
    }

    public RelationBuilder entityBTechId(String entityBTechId) {
        this.entityBTechId = entityBTechId;
        return this;
    }

    public RelationBuilder ref(String... ref) {
        this.refs = Arrays.asList(ref);
        return this;
    }

    public RelationBuilder reference(RefBuilder ref) {
        refs.add(ref.id());
        //add as entities sub resource
        subEntities.add(ref.toEntity());
        //add a relation
        hasRefs.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(ref.id())
                .withETag1(getETag())
                .withETag2(ref.getETag())
                .withRType("hasRelationReference")
                .build());
        return this;
    }

    public RelationBuilder value(RvalueBuilder... value) {
        Arrays.asList(value).forEach(this::value);
        return this;
    }

    public RelationBuilder value(RvalueBuilder value) {
        value.relationId = this.id();
        value.context = this.context;

        //add as entities sub resource
        subEntities.add(value.toEntity());
        //add a relation
        hasValues.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(value.id())
                .withETag1(getETag())
                .withETag2(value.getETag())
                .withRType("hasRvalue")
                .build());

        return this;
    }

    public List<Relationship> withRelations() {
        return withRelations(o -> true);
    }

    public List<Relationship> withRelations(String relationType, String... outSideId) {
        return withRelations(p -> p.getrType().equals(relationType) && Arrays.asList(outSideId).contains(p.geteID2()));
    }

    public List<Relationship> withRelations(Predicate<Relationship> filter) {
        return Stream.ofAll(hasValues)
                .appendAll(hasRefs)
                .filter(filter)
                .toJavaList();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return relId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create knowledge entity
        on.put("id", id());
        on.put("type", physicalType);
        on.put("context", context);
        on.put("category", category);
        on.put("techId", techId);
        on.put("entityAId", entityAId);
        on.put("entityBId", entityBId);
        on.put("entityACategory", entityACategory);
        on.put("entityATechId", entityATechId);
        on.put("entityBCategory", entityBCategory);
        on.put("entityBTechId", entityBTechId);
        on.put("refs", collectRefs(mapper, refs));
        //make sure value or content
        return on;
    }

    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        new Property("context", "raw", context),
                        new Property("category", "raw", category),
                        new Property("techId", "raw", techId),
                        new Property("entityAId", "raw", entityAId),
                        new Property("entityBId", "raw", entityBId),
                        new Property("entityACategory", "raw", entityACategory),
                        new Property("entityBCategory", "raw", entityBCategory),
                        new Property("refs", "raw", !refs.isEmpty() ? refs : null)
                        ), additionalProperties.entrySet().stream()
                                .map(p -> new Property(p.getKey(), p.getValue())).collect(Collectors.toList()))
                ).build();
    }

    @Override
    public String getETag() {
        return "Reference." + id();
    }


    public static class EntityRelationBuilder extends KnowledgeDomainBuilder {
        public static String physicalType = "e.relation";
        private RelationBuilder builder;
        private String dir;

        public EntityRelationBuilder(String entityAId, RelationBuilder source, String dir) {
            this.builder = new RelationBuilder(source);
            this.dir = dir;
            //
            if (!builder.entityAId.equals(entityAId)) {
                String entityACategory = builder.entityACategory;
                String entityATechId= builder.entityATechId;

                builder.entityACategory(builder.entityBCategory);
                builder.entityATechId(builder.entityBTechId);
                builder.entityBCategory(entityACategory);
                builder.entityBTechId(entityATechId);

                builder.entityBId(builder.entityAId);
                builder.entityAId(entityAId);
            }
        }

        @Override
        public String getType() {
            return physicalType;
        }

        @Override
        public String id() {
            return builder.id() + "." + dir;
        }

        @Override
        public Entity toEntity() {
            return null;
        }

        @Override
        public String getETag() {
            return null;
        }

        @Override
        public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
            builder.collect(mapper, node);
            node.put("id", id());
            node.put("type", physicalType);
            node.put("direction", dir);
            node.put("relationId", builder.id());
            return node;
        }

        @Override
        public Optional<String> routing() {
            try {
                return Optional.of(this.builder.entityAId.split("\\.")[0]);
            }catch (Throwable err) {
                throw new RuntimeException(err);
            }
        }
    }

}
