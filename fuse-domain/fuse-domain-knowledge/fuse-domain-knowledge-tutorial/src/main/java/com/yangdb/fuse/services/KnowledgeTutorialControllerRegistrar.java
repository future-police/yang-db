package com.yangdb.fuse.services;

/*-
 * #%L
 * fuse-domain-knowledge-tutorial
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
import com.yangdb.fuse.services.appRegistrars.AppControllerRegistrarBase;
import org.jooby.Jooby;
import org.jooby.Results;

public class KnowledgeTutorialControllerRegistrar extends AppControllerRegistrarBase<KnowledgeTutorialControllerRegistrar> {

    //region Constructors
    public KnowledgeTutorialControllerRegistrar() {
        super(KnowledgeTutorialControllerRegistrar.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("tutorial/1", () -> Results.redirect("/public/assets/swagger/swagger-tutorial-1.json"));
        app.get("tutorial/2", () -> Results.redirect("/public/assets/swagger/swagger-tutorial-2.json"));
    }
    //endregion
}
