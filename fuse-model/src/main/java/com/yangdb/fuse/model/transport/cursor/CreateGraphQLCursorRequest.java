package com.yangdb.fuse.model.transport.cursor;

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
 * CreateGraphCursorRequest.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphQLCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "graphQL";

    //region Constructors
    public CreateGraphQLCursorRequest() {
        super(CursorType);
    }

    public CreateGraphQLCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateGraphQLCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }

    public CreateGraphQLCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest) {
        super(cursorType,include,createPageRequest);
    }

    public CreateGraphQLCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest, GraphFormat format) {
        super(cursorType,include,createPageRequest);
        this.format = format;
    }

    public GraphFormat getFormat() {
        return format;
    }

    //endregion
    private GraphFormat format = GraphFormat.JSON;

    public enum GraphFormat {
        JSON,XML
    }
    //endregion
}
