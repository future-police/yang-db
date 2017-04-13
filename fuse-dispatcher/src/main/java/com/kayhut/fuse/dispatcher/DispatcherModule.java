package com.kayhut.fuse.dispatcher;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.processor.PageProcessor;
import com.kayhut.fuse.dispatcher.context.processor.ResourcePersistProcessor;
import com.kayhut.fuse.dispatcher.driver.*;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.InMemoryResourceStore;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import static com.kayhut.fuse.model.Utils.baseUrl;

/**
 * Created by lior on 15/02/2017.
 *
 * This module is called by the fuse-service scanner class loader
 */
public class DispatcherModule implements Jooby.Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(new DefaultAppUrlSupplier(baseUrl(conf.getString("application.port"))));

        // resource store and persist processor
        binder.bind(ResourceStore.class).to(InMemoryResourceStore.class).asEagerSingleton();
        binder.bind(OntologyProvider.class).to(SimpleOntologyProvider.class).asEagerSingleton();
        binder.bind(ResourcePersistProcessor.class).asEagerSingleton();

        // page processor
        binder.bind(PageCreationOperationContext.Processor.class).to(PageProcessor.class).asEagerSingleton();

        // service controllers
        binder.bind(QueryDispatcherDriver.class).to(SimpleQueryDispatcherDriver.class).asEagerSingleton();
        binder.bind(CursorDispatcherDriver.class).to(SimpleCursorDispatcherDriver.class).asEagerSingleton();
        binder.bind(PageDispatcherDriver.class).to(SimplePageDispatcherDriver.class).asEagerSingleton();
    }

}
