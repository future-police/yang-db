package com.yangdb.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

/**
 * Created by lior.perry pc on 5/12/2018.
 */
public class KnowledgeRelationEntity {
    private RelationEntity _relEntity;
    private Relation _relaton;
    private Entity _aEntity, _bEntity;

    public void setRelation(Relation r) {
        _relaton = r;
    }

    public void setRelationEntities(Entity a, Entity b) {
        _aEntity = a;
        _bEntity = b;
    }
}