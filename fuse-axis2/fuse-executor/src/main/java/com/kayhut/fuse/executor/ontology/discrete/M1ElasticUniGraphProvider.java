package com.kayhut.fuse.executor.ontology.discrete;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteElementVertexController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseElementEdgeController;
import com.kayhut.fuse.unipop.controller.promise.PromiseElementVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexFilterController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.elasticsearch.client.Client;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by Roman on 06/04/2017.
 */
public class M1ElasticUniGraphProvider implements UniGraphProvider {
    //region Constructors

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public M1ElasticUniGraphProvider(
            Client client,
            ElasticGraphConfiguration elasticGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.client = client;
        this.elasticGraphConfiguration = elasticGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        return new UniGraph(
                this.uniGraphConfiguration,
                controllerManagerFactory(schemaProviderFactory.get(ontology)),
                new StandardStrategyProvider());
    }

    //region Private Methods
    /**
     * default controller Manager
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider) {
        return uniGraph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new DiscreteElementVertexController(
                                        client,
                                        elasticGraphConfiguration,
                                        uniGraph,
                                        schemaProvider,
                                        new MetricRegistry()),
                                null,
                                new MetricRegistry()
                        ),
                        new DiscreteVertexController(
                                client,
                                elasticGraphConfiguration,
                                uniGraph,
                                schemaProvider,
                                new MetricRegistry())
                );
            }

            @Override
            public void close() {

            }
        };
    }
    //endregion

    //region Fields
    private final Client client;
    private final ElasticGraphConfiguration elasticGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}