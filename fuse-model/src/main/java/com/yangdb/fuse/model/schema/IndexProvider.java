package com.yangdb.fuse.model.schema;

/*-
 * #%L
 * fuse-model
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


/*-
 *
 * Ontology.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "entities",
        "relations"
})
public class IndexProvider {

    @JsonProperty("ontology")
    private String ontology;
    @JsonProperty("entities")
    private List<Entity> entities = null;
    @JsonProperty("relations")
    private List<Relation> relations = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("entities")
    public List<Entity> getEntities() {
        return Stream.concat(entities.stream()
                    .filter(e -> !e.getNested().isEmpty())
                    .flatMap(e -> e.getNested().stream()),entities.stream())
                .collect(Collectors.toList());
    }

    @JsonProperty("entities")
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @JsonProperty("relations")
    public List<Relation> getRelations() {
        return Stream.concat(relations.stream()
                .filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream()),relations.stream())
                .collect(Collectors.toList());
    }

    @JsonProperty("relations")
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    @JsonProperty("ontology")
    public String getOntology() {
        return ontology;
    }

    @JsonProperty("ontology")
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonIgnore
    public Optional<Entity> getEntity(String label) {
        Optional<Entity> nest = getEntities().stream().filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream())
                .filter(nested -> nested.getType().equals(label))
                .findAny();
        if (nest.isPresent())
            return nest;

        return getEntities().stream().filter(e -> e.getType().equals(label)).findAny();
    }

    @JsonIgnore
    public Optional<Relation> getRelation(String label) {
        Optional<Relation> nest = getRelations().stream().filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream())
                .filter(nested -> nested.getType().equals(label))
                .findAny();
        if (nest.isPresent())
            return nest;

        return getRelations().stream().filter(e -> e.getType().equals(label)).findAny();
    }
}
