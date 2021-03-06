package com.yangdb.fuse.services.appRegistrars;

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

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.services.controllers.InternalsController;
import org.jooby.Jooby;
import org.jooby.Results;

public class InternalsControllerRegistrar extends AppControllerRegistrarBase<InternalsController> {
    //region Constructors
    public InternalsControllerRegistrar() {
        super(InternalsController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.get("/fuse/internal/statisticsProvider/name",
                req -> Results.with(this.getController(app).getStatisticsProviderName()));
        app.get("/fuse/internal/version",
                req -> Results.with(this.getController(app).getVersion()));
        app.get("/fuse/internal/config",
                req -> Results.with(this.getController(app).getConfig()));
        app.get("/fuse/internal/snowflakeId",
                req -> Results.with(this.getController(app).getSnowflakeId()));
        app.get("/fuse/internal/cursorBindings",
                req -> Results.with(this.getController(app).getCursorBindings()));
        app.get("/fuse/internal/statisticsProvider/setup",
                req -> Results.with(this.getController(app).getStatisticsProviderSetup()));
        app.put("/fuse/internal/statisticsProvider/refresh",
                req -> Results.with(this.getController(app).refreshStatisticsProviderSetup()));
    }
    //endregion
}
