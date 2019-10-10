package com.yangdb.fuse.dispatcher.modules;

/*-
 *
 * fuse-core
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

import com.google.inject.Binder;
import com.yangdb.fuse.dispatcher.ontology.*;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.jooby.Env;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * Created by lior.perry on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class NewDispatcherModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(getAppUrlSupplier(conf));
        binder.bind(OntologyProvider.class).toInstance(getOntologyProvider(conf));
        binder.bind(OntologyTransformerProvider.class).toInstance(getTransformerProvider(conf));
    }

    //region Private Methods
    private AppUrlSupplier getAppUrlSupplier(Config conf) throws UnknownHostException {
        int applicationPort = conf.getInt("application.port");
        String baseUrl = String.format("http://%s:%d/fuse", InetAddress.getLocalHost().getHostAddress(), applicationPort);
        if (conf.hasPath("appUrlSupplier.public.baseUri")) {
            baseUrl = conf.getString("appUrlSupplier.public.baseUri");
        }

        return new DefaultAppUrlSupplier(baseUrl);
    }

    private OntologyProvider getOntologyProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return new DirectoryOntologyProvider(conf.getString("fuse.ontology_provider_dir"));
        } catch (ConfigException e) {
            return (OntologyProvider) Class.forName(conf.getString("fuse.ontology_provider")).getConstructor().newInstance();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //no ontology provider was found
    }

    private OntologyTransformerProvider getTransformerProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return new DirectoryOntologyTransformerProvider(conf.getString("fuse.ontology_provider_dir"));
        } catch (ConfigException e) {
            try {
                return (OntologyTransformerProvider) Class.forName(conf.getString("fuse.ontology_transformation_provider")).getConstructor().newInstance();
            } catch (ConfigException.Missing missing) {
                return new VoidOntologyTransformerProvider();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //no ontology provider was found
    }
    //endregion
}
