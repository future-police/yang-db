package com.kayhut.fuse.asg.strategy;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class ValidatorStrategyRegisteredAsgDriver implements QueryValidationOperationContext.Processor {

    //region Constructors
    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ValidatorStrategyRegisteredAsgDriver(
            EventBus eventBus,
            AsgValidatorStrategyRegistrar registrar,
            OntologyProvider ontologyProvider) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.ontologyProvider = ontologyProvider;

        this.strategies = registrar.register();
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public ValidationContext process(QueryValidationOperationContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(context.getQuery().getOnt());
        if (!ontology.isPresent()) {
            throw new RuntimeException("No ontology provided");
        }
        try {

            Timer.Context time = metricRegistry.timer(
                    name(QueryCreationOperationContext.class.getSimpleName(),
                            context.getQueryMetadata().getId(),
                            ValidatorStrategyRegisteredAsgDriver.class.getSimpleName())).time();


            AsgStrategyContext asgStrategyContext = new AsgStrategyContext(new Ontology.Accessor(ontology.get()));
            AsgQuery asgQuery = new AsgQuerySupplier(context.getQuery(), new NextEbaseFactory(), new BNextFactory()).get();
            Stream<ValidationContext> validationContextStream = Stream.ofAll(this.strategies).toStream().map(strategy -> strategy.apply(asgQuery, asgStrategyContext));

            time.stop();
            //if valid continue flow - other return error to client
            if (validationContextStream.toJavaStream().anyMatch(p -> !p.valid())) {
                List<String> errors = validationContextStream.toJavaStream().filter(p -> !p.valid())
                        .flatMap(k -> Arrays.stream(k.errors())).collect(Collectors.toList());
                return new ValidationContext(false, errors.toArray(new String[errors.size()]));
            } else {
                submit(eventBus, new QueryCreationOperationContext(context.getQueryMetadata(), context.getQuery()));
                return ValidationContext.OK;
            }
        } catch (Exception e) {
            return new ValidationContext(false, "Query not valid " + e.getMessage());
        }
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private Iterable<AsgValidatorStrategy> strategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
