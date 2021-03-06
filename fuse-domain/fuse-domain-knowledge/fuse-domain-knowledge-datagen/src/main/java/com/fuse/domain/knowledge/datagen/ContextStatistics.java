package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContextStatistics {
    //region Constructors
    public ContextStatistics() {
        this.entityCategories = Collections.emptyMap();
        this.relationCategories = Collections.emptyMap();

        this.entityRelationCategories = Collections.emptyMap();

        this.entityCategoryFields = Collections.emptyMap();
        this.relationCategoryFields = Collections.emptyMap();

        this.entityValueCounts = Collections.emptyMap();
        this.entityGlobalValueCounts = Collections.emptyMap();
        this.relationValueCounts = Collections.emptyMap();

        this.entityRelationCounts = Collections.emptyMap();
        this.insightEntityCounts = Collections.emptyMap();

        this.entityReferenceCounts = Collections.emptyMap();
        this.entityValueReferenceCounts = Collections.emptyMap();
        this.relationReferenceCounts = Collections.emptyMap();
        this.relationValueReferenceCounts = Collections.emptyMap();
        this.insightReferenceCounts = Collections.emptyMap();
    }
    //endregion

    //region Properties
    public Map<String, Integer> getEntityCategories() {
        return entityCategories;
    }

    public void setEntityCategories(Map<String, Integer> entityCategories) {
        this.entityCategories = entityCategories;
    }

    public Map<String, Integer> getRelationCategories() {
        return relationCategories;
    }

    public void setRelationCategories(Map<String, Integer> relationCategories) {
        this.relationCategories = relationCategories;
    }

    public Map<String, Set<String>> getEntityRelationCategories() {
        return entityRelationCategories;
    }

    public void setEntityRelationCategories(Map<String, Set<String>> entityRelationCategories) {
        this.entityRelationCategories = entityRelationCategories;
    }

    public Map<String, Set<String>> getEntityCategoryFields() {
        return entityCategoryFields;
    }

    public void setEntityCategoryFields(Map<String, Set<String>> entityCategoryFields) {
        this.entityCategoryFields = entityCategoryFields;
    }

    public Map<String, Set<String>> getRelationCategoryFields() {
        return relationCategoryFields;
    }

    public void setRelationCategoryFields(Map<String, Set<String>> relationCategoryFields) {
        this.relationCategoryFields = relationCategoryFields;
    }

    public Map<String, String> getEntityFieldTypes() {
        return entityFieldTypes;
    }

    public Map<String, String> getRelationFieldTypes() {
        return relationFieldTypes;
    }

    public void setRelationFieldTypes(Map<String, String> relationFieldTypes) {
        this.relationFieldTypes = relationFieldTypes;
    }

    public void setEntityFieldTypes(Map<String, String> entityFieldTypes) {
        this.entityFieldTypes = entityFieldTypes;
    }

    public Map<String, Map<Integer, Integer>> getEntityValueCounts() {
        return entityValueCounts;
    }

    public void setEntityValueCounts(Map<String, Map<Integer, Integer>> entityValueCounts) {
        this.entityValueCounts = entityValueCounts;
    }

    public Map<String, Map<Integer, Integer>> getEntityGlobalValueCounts() {
        return entityGlobalValueCounts;
    }

    public void setEntityGlobalValueCounts(Map<String, Map<Integer, Integer>> entityGlobalValueCounts) {
        this.entityGlobalValueCounts = entityGlobalValueCounts;
    }

    public Map<String, Map<Integer, Integer>> getRelationValueCounts() {
        return relationValueCounts;
    }

    public void setRelationValueCounts(Map<String, Map<Integer, Integer>> relationValueCounts) {
        this.relationValueCounts = relationValueCounts;
    }

    public Map<String, Map<Integer, Integer>> getEntityRelationCounts() {
        return entityRelationCounts;
    }

    public void setEntityRelationCounts(Map<String, Map<Integer, Integer>> entityRelationCounts) {
        this.entityRelationCounts = entityRelationCounts;
    }

    public Map<Integer, Integer> getInsightEntityCounts() {
        return insightEntityCounts;
    }

    public void setInsightEntityCounts(Map<Integer, Integer> insightEntityCounts) {
        this.insightEntityCounts = insightEntityCounts;
    }

    public Map<Integer, Integer> getEntityReferenceCounts() {
        return entityReferenceCounts;
    }

    public void setEntityReferenceCounts(Map<Integer, Integer> entityReferenceCounts) {
        this.entityReferenceCounts = entityReferenceCounts;
    }

    public Map<Integer, Integer> getEntityValueReferenceCounts() {
        return entityValueReferenceCounts;
    }

    public void setEntityValueReferenceCounts(Map<Integer, Integer> entityValueReferenceCounts) {
        this.entityValueReferenceCounts = entityValueReferenceCounts;
    }

    public Map<Integer, Integer> getEntityGlobalValueReferenceCounts() {
        return entityGlobalValueReferenceCounts;
    }

    public void setEntityGlobalValueReferenceCounts(Map<Integer, Integer> entityGlobalValueReferenceCounts) {
        this.entityGlobalValueReferenceCounts = entityGlobalValueReferenceCounts;
    }

    public Map<Integer, Integer> getRelationReferenceCounts() {
        return relationReferenceCounts;
    }

    public void setRelationReferenceCounts(Map<Integer, Integer> relationReferenceCounts) {
        this.relationReferenceCounts = relationReferenceCounts;
    }

    public Map<Integer, Integer> getRelationValueReferenceCounts() {
        return relationValueReferenceCounts;
    }

    public void setRelationValueReferenceCounts(Map<Integer, Integer> relationValueReferenceCounts) {
        this.relationValueReferenceCounts = relationValueReferenceCounts;
    }

    public Map<Integer, Integer> getInsightReferenceCounts() {
        return insightReferenceCounts;
    }

    public void setInsightReferenceCounts(Map<Integer, Integer> insightReferenceCounts) {
        this.insightReferenceCounts = insightReferenceCounts;
    }

    public long getDistinctNumReferences() {
        return distinctNumReferences;
    }

    public void setDistinctNumReferences(long distinctNumReferences) {
        this.distinctNumReferences = distinctNumReferences;
    }
    //endregion

    //region Fields
    private Map<String, Integer> entityCategories;
    private Map<String, Integer> relationCategories;

    private Map<String, Set<String>> entityRelationCategories;

    private Map<String, String> entityFieldTypes;
    private Map<String, String> relationFieldTypes;

    private Map<String, Set<String>> entityCategoryFields;
    private Map<String, Set<String>> relationCategoryFields;

    private Map<String, Map<Integer, Integer>> entityValueCounts;
    private Map<String, Map<Integer, Integer>> entityGlobalValueCounts;
    private Map<String, Map<Integer, Integer>> relationValueCounts;

    private Map<String, Map<Integer, Integer>> entityRelationCounts;

    private Map<Integer, Integer> insightEntityCounts;

    private Map<Integer, Integer> entityReferenceCounts;
    private Map<Integer, Integer> entityValueReferenceCounts;
    private Map<Integer, Integer> entityGlobalValueReferenceCounts;
    private Map<Integer, Integer> relationReferenceCounts;
    private Map<Integer, Integer> relationValueReferenceCounts;
    private Map<Integer, Integer> insightReferenceCounts;

    private long distinctNumReferences;
    //endregion
}
