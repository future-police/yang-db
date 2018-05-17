package com.kayhut.fuse.executor;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.driver.StandardCursorDriver;
import com.kayhut.fuse.executor.driver.StandardPageDriver;
import com.kayhut.fuse.executor.driver.StandardQueryDriver;
import com.kayhut.fuse.executor.elasticsearch.ClientProvider;
import com.kayhut.fuse.executor.elasticsearch.TimeoutClientAdvisor;
import com.kayhut.fuse.executor.elasticsearch.logging.LoggingClient;
import com.kayhut.fuse.executor.logging.LoggingCursorFactory;
import com.kayhut.fuse.executor.logging.LoggingGraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.executor.ontology.schema.*;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProvider;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.typesafe.config.Config;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.elasticsearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.configuration.UniGraphConfiguration;

import java.util.List;

import static com.google.inject.name.Names.named;

/**
 * Created by lior on 22/02/2017.
 */
public class ExecutorModule extends ModuleBase {
    //region Jooby.Module Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {

        bindInitialDataLoader(env, conf, binder);
        bindCursorFactory(env, conf, binder);
        bindElasticClient(env, conf, binder);
        bindRawSchema(env, conf, binder);
        bindSchemaProviderFactory(env, conf, binder);
        bindUniGraphProvider(env, conf, binder);

        binder.bind(QueryDriver.class).to(StandardQueryDriver.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(StandardCursorDriver.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(StandardPageDriver.class).in(RequestScoped.class);
        binder.bind(SearchOrderProviderFactory.class).to(getSearchOrderProvider(conf));
    }

    //endregion

    //region Private Methods

    protected void bindInitialDataLoader(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(InitialGraphDataLoader.class)
                            .to(getInitialDataLoader(conf));
                    this.expose(InitialGraphDataLoader.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindRawSchema(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PrefixedRawSchema.rawSchemaParameter))
                            .to(getRawElasticSchemaClass(conf))
                            .asEagerSingleton();

                    String prefix = conf.hasPath(conf.getString("assembly") + ".physical_raw_schema_prefix") ?
                            conf.getString(conf.getString("assembly") + ".physical_raw_schema_prefix") :
                            "";
                    this.bindConstant().annotatedWith(named(PrefixedRawSchema.prefixParameter)).to(prefix);
                    this.bind(RawSchema.class)
                            .annotatedWith(named(PartitionFilteredRawSchema.rawSchemaParameter))
                            .to(PrefixedRawSchema.class)
                            .asEagerSingleton();

                    this.bind(RawSchema.class)
                            .annotatedWith(named(CachedRawSchema.rawSchemaParameter))
                            .to(PartitionFilteredRawSchema.class);

                    this.bind(RawSchema.class).to(CachedRawSchema.class);

                    this.expose(RawSchema.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindCursorFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(CursorFactory.class)
                            .annotatedWith(named(LoggingCursorFactory.cursorFactoryParameter))
                            .to(getCursorFactoryClass(conf))
                            .asEagerSingleton();
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingCursorFactory.cursorLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(Cursor.class));
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingCursorFactory.traversalLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(Traversal.class));
                    this.bind(CursorFactory.class)
                            .to(LoggingCursorFactory.class)
                            .asEagerSingleton();

                    this.expose(CursorFactory.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindElasticClient(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                boolean createMock = conf.hasPath("fuse.elasticsearch.mock") && conf.getBoolean("fuse.elasticsearch.mock");
                this.bind(ElasticGraphConfiguration.class).toInstance(createElasticGraphConfiguration(conf));

                this.bindConstant()
                        .annotatedWith(named(ClientProvider.createMockParameter))
                        .to(createMock);
                this.bind(Client.class)
                        .annotatedWith(named(LoggingClient.clientParameter))
                        .toProvider(ClientProvider.class).asEagerSingleton();
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingClient.class));
                this.bind(Client.class)
                        .to(TimeoutClientAdvisor.class)
                        .in(RequestScoped.class);

                this.expose(Client.class);
                this.expose(ElasticGraphConfiguration.class);
            }
        });
    }

    protected void bindSchemaProviderFactory(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(OntologyGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(getSchemaProviderFactoryClass(conf));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.schemaProviderFactoryParameter))
                            .to(OntologyGraphElementSchemaProviderFactory.class);
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.warnLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class));
                    this.bind(Logger.class)
                            .annotatedWith(named(LoggingGraphElementSchemaProviderFactory.verboseLoggerParameter))
                            .toInstance(LoggerFactory.getLogger(GraphElementSchemaProvider.class.getName() + ".Verbose"));
                    this.bind(GraphElementSchemaProviderFactory.class)
                            .to(LoggingGraphElementSchemaProviderFactory.class);

                    this.expose(GraphElementSchemaProviderFactory.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void bindUniGraphProvider(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                try {
                    this.bind(UniGraphConfiguration.class).toInstance(createUniGraphConfiguration(conf));
                    this.bind(UniGraphProvider.class)
                            .to(getUniGraphProviderClass(conf))
                            .in(RequestScoped.class);

                    this.expose(UniGraphProvider.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Class<? extends RawSchema> getRawElasticSchemaClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly")+".physical_raw_schema"));
    }

    private Class<? extends InitialGraphDataLoader> getInitialDataLoader(Config conf) throws ClassNotFoundException {
        return (Class<? extends InitialGraphDataLoader>) (Class.forName(conf.getString(conf.getString("assembly")+".physical_schema_data_loader")));
    }

    private Class<? extends SearchOrderProviderFactory> getSearchOrderProvider(Config conf) throws ClassNotFoundException {
        return (Class<? extends SearchOrderProviderFactory>) (Class.forName(conf.getString(conf.getString("assembly")+".search_order_provider")));
    }

    private ElasticGraphConfiguration createElasticGraphConfiguration(Config conf) {
        ElasticGraphConfiguration configuration = new ElasticGraphConfiguration();
        configuration.setClusterHosts(Stream.ofAll(getStringList(conf, "elasticsearch.hosts")).toJavaArray(String.class));
        configuration.setClusterPort(conf.getInt("elasticsearch.port"));
        configuration.setClusterName(conf.getString("elasticsearch.cluster_name"));
        configuration.setElasticGraphDefaultSearchSize(conf.getLong("elasticsearch.default_search_size"));
        configuration.setElasticGraphMaxSearchSize(conf.getLong("elasticsearch.max_search_size"));
        configuration.setElasticGraphScrollSize(conf.getInt("elasticsearch.scroll_size"));
        configuration.setElasticGraphScrollTime(conf.getInt("elasticsearch.scroll_time"));
        return configuration;
    }

    private UniGraphConfiguration createUniGraphConfiguration(Config conf) {
        UniGraphConfiguration configuration = new UniGraphConfiguration();
        configuration.setBulkMax(conf.getInt("unipop.bulk.max"));
        configuration.setBulkStart(conf.getInt("unipop.bulk.start"));
        configuration.setBulkMultiplier(conf.getInt("unipop.bulk.multiplier"));
        return configuration;
    }

    protected Class<? extends GraphElementSchemaProviderFactory> getSchemaProviderFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends GraphElementSchemaProviderFactory>) Class.forName(conf.getString(conf.getString("assembly")+".physical_schema_provider_factory_class"));
    }

    protected Class<? extends UniGraphProvider> getUniGraphProviderClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  UniGraphProvider>)Class.forName(conf.getString(conf.getString("assembly")+".unigraph_provider"));
    }

    protected Class<? extends CursorFactory> getCursorFactoryClass(Config conf) throws ClassNotFoundException {
        return (Class<? extends  CursorFactory>)Class.forName(conf.getString(conf.getString("assembly")+".cursor_factory"));
    }

    private List<String> getStringList(Config conf, String key) {
         try {
             return conf.getStringList(key);
         } catch (Exception ex) {
             String strList = conf.getString(key);
             return Stream.of(strList.split(",")).toJavaList();
         }
    }
    //endregion
}
