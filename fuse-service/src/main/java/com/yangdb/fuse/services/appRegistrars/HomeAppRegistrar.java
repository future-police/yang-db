package com.yangdb.fuse.services.appRegistrars;

/*-
 *
 * fuse-service
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

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;
import org.jooby.Result;
import org.jooby.Results;

public class HomeAppRegistrar implements AppRegistrar{
    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/", () -> HOME);
    }
    //endregion

    //region Fields
    private static Result HOME = Results
            .ok(
                    "<!doctype html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "  <title>Fuse API</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<h1>Fuse API</h1>\n" +
                            "<ul>\n" +
                            "<li>Resource Url: <a href=\"/fuse\">fuse</a></li>\n" +
                            "<li>Health Url: <a href=\"/fuse/health\">healthUrl</a></li>\n" +
                            "<li>Query Store Url: <a href=\"/fuse/query\">queryStoreUrl</a></li>\n" +
                            "<li>Search Store Url: <a href=\"/fuse/search\">searchStoreUrl</a></li>\n" +
                            "<li>Catalog Store Url: <a href=\"/fuse/catalog\">catalogStoreUrl</a></li>\n" +
                            "</ul>\n" +
                            "<p>More at <a href=\"http://sheker.com\">" +
                            "Sheker</a>\n" +
                            "</body>\n" +
                            "</html>")
            .type("html");
    //endregion
}
