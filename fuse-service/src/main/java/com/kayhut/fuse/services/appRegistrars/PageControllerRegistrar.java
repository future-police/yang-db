package com.kayhut.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
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

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.logging.Route;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import org.jooby.Jooby;
import org.jooby.Results;

public class PageControllerRegistrar extends AppControllerRegistrarBase<PageController> {
    //region Constructors
    public PageControllerRegistrar() {
        super(PageController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the cursor page store info*/
        app.get(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("getPageStore").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status());
                });

        /** create the next page */
        app.post(appUrlSupplier.pageStoreUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("postPage").write();
                    CreatePageRequest createPageRequest = req.body(CreatePageRequest.class);
                    req.set(CreatePageRequest.class, createPageRequest);
                    ContentResponse<PageResourceInfo> entity = createPageRequest.isFetch() ?
                            this.getController(app).createAndFetch(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest) :
                            this.getController(app).create(req.param("queryId").value(), req.param("cursorId").value(), createPageRequest);
                    return Results.with(entity, entity.status());
                });

        /** view the elastic query with d3 html*/
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/elastic/view",
                req -> Results.redirect("/public/assets/ElasticQueryViewer.html?q=" +
                        appUrlSupplier.pageStoreUrl(req.param("queryId").value(), req.param("cursorId").value()) + "/" + req.param("pageId").value() + "/elastic"));


        /** get page info by id */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> {
                    Route.of("getPage").write();
                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId") + "/data",
                req -> {
                    Route.of("getPageData").write();

                    ContentResponse response = this.getController(app).getData(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });

        /** get page data by id */
        app.delete(appUrlSupplier.resourceUrl(":queryId", ":cursorId", ":pageId"),
                req -> {
                    Route.of("deletePage").write();

                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value(), req.param("pageId").value());
                    return Results.with(response, response.status());
                });
    }
    //endregion
}
