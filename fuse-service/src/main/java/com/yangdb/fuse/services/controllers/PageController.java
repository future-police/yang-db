package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface PageController<C,D> extends Controller<C,D>{
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest);
    ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId);
    ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId);
    ContentResponse<Object> getData(String queryId, String cursorId, String pageId);
    ContentResponse<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format);
}
