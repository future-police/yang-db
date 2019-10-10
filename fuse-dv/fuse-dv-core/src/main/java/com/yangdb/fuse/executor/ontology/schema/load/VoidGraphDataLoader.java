package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 *
 * fuse-dv-core
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

import com.yangdb.fuse.model.logical.LogicalGraphModel;

import java.io.File;
import java.io.IOException;

public class VoidGraphDataLoader implements GraphDataLoader {
    //region GraphDataLoader Implementation
    @Override
    public long init() throws IOException {
        return 0;
    }

    @Override
    public LoadResponse load(LogicalGraphModel root, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse load(File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }
    //endregion
}
