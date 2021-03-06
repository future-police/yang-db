package com.yangdb.fuse.unipop.controller.promise.context;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.common.context.ElementControllerContext;
import com.yangdb.fuse.unipop.promise.*;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by lior.perry on 27/03/2017.
 */
public class PromiseElementControllerContext extends ElementControllerContext.Impl {

    //region Constructors
    public PromiseElementControllerContext(
            UniGraph graph,
            StepDescriptor stepDescriptor,
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            Iterable<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            int limit) {
        super(graph,stepDescriptor, elementType, schemaProvider, constraint, selectPHasContainers, limit);
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    //endregion

}
