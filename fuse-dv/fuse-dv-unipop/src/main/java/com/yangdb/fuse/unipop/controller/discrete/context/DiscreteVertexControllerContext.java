package com.yangdb.fuse.unipop.controller.discrete.context;

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

import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class DiscreteVertexControllerContext extends VertexControllerContext.Impl {
    public DiscreteVertexControllerContext(UniGraph graph, StepDescriptor stepDescriptor, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit, Direction direction, Iterable<Vertex> bulkVertices) {
        super(graph,stepDescriptor, ElementType.edge, schemaProvider, constraint, selectPHasContainers, limit, direction, bulkVertices);
    }
}
