package com.kayhut.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by Roman on 06/04/2017.
 */
public class SimplePhysicalIndexProvider implements PhysicalIndexProvider {
    //region Constructors
    public SimplePhysicalIndexProvider(String vertexIndexName, String edgeIndexName) {
        this.vertexIndexName = vertexIndexName;
        this.edgeIndexName = edgeIndexName;
    }
    //endregion

    //region PhysicalIndexProvider Implementation
    @Override
    public IndexPartitions getIndexPartitionsByLabel(String label, ElementType elementType) {
        switch (elementType) {
            case edge: return new StaticIndexPartitions(Collections.singletonList(this.edgeIndexName));
            case vertex: return new StaticIndexPartitions(Collections.singletonList(this.vertexIndexName));
            default: return null;
        }
    }
    //endregion

    //region Fields
    private String vertexIndexName;
    private String edgeIndexName;
    //endregion
}
