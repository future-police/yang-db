package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class ExecutorModule implements Jooby.Module  {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PageCreationOperationContext.Processor.class).to(PageProcessor.class).asEagerSingleton();
    }
}
