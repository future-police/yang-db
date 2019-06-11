package com.kayhut.fuse.graph;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.services.controller.PocGraphController;
import com.kayhut.fuse.services.controller.StandardPocGraphController;
import com.typesafe.config.Config;
import org.graphstream.graph.Graph;
import org.jooby.Env;

import java.time.Duration;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class PocModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        bindController(env, conf, binder);
    }

    private void bindController(Env env, Config config, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<Cache<String, Graph>>() {})
                        .toInstance(Caffeine.newBuilder()
                                .expireAfterAccess(Duration.ofMinutes(10))
                                .maximumSize(10000)
                                .build());
                this.bind(PocGraphController.class)
                        .to(StandardPocGraphController.class);
                this.expose(PocGraphController.class);
            }
        });
    }
    //endregion
}
