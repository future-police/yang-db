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

import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.services.controllers.IdGeneratorController;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.Arrays;

public class IdGeneratorControllerRegistrar extends AppControllerRegistrarBase<IdGeneratorController<Range>> {
    //region Constructors
    public IdGeneratorControllerRegistrar() {
        super(new TypeLiteral<IdGeneratorController<Range>>(){});
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/fuse/idgen/init",
                req -> Results.with(this.getController(app).init(Arrays.asList(req.param("names").value().split(",")))));
        app.get("/fuse/idgen",
                req -> this.getController(app).getNext(req.param("id").value(), req.param("numIds").intValue()));
    }

    //endregion
}
