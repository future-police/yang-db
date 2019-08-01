package com.yangdb.fuse.services.module;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.resource.store.InMemoryResourceStore;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.executor.cursor.discrete.GraphTraversalCursor;
import com.yangdb.fuse.executor.cursor.discrete.PathsTraversalCursor;
import com.yangdb.fuse.executor.mock.elasticsearch.MockClient;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.GraphDataLoader;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.yangdb.fuse.services.dispatcher.driver.MockDriver;
import com.yangdb.fuse.services.engine2.data.schema.InitialTestDataLoader;
import com.yangdb.fuse.services.engine2.data.schema.discrete.M2DragonsPhysicalSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.typesafe.config.Config;
import org.elasticsearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

/**
 * Created by Roman on 04/04/2017.
 */
public class DriverTestModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(ResourceStore.class).toInstance(new InMemoryResourceStore());
        binder.bind(QueryDriver.class).to(MockDriver.Query.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(MockDriver.Cursor.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(MockDriver.Page.class).in(RequestScoped.class);

        binder.bind(GraphElementSchemaProviderFactory.class)
                .toInstance(ontology -> new OntologySchemaProvider(ontology, new M2DragonsPhysicalSchemaProvider()));
        binder.bind(Client.class).toInstance(new MockClient());
        binder.bind(GraphDataLoader.class).toInstance(new InitialTestDataLoader(null, null));

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new PathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreateGraphCursorRequest.CursorType,
                CreateGraphCursorRequest.class,
                new GraphTraversalCursor.Factory()));
    }

}