package com.kayhut.fuse.gta;

import com.google.inject.Binder;
import com.kayhut.fuse.dispatcher.ModuleBase;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Created by lior on 22/02/2017.
 */
public class GtaModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(GremlinTranslator.class).to(GremlinTranslationAppenderEngine.class).asEagerSingleton();
        binder.bind(CursorCreationOperationContext.Processor.class).to(GtaTraversalCursorProcessor.class).asEagerSingleton();
    }
}
