package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgValidatorStrategy;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class CompositeValidatorStrategy implements AsgValidatorStrategy {
    //region Constructors
    public CompositeValidatorStrategy(AsgValidatorStrategy...strategies) {
        this(Stream.of(strategies));
    }

    public CompositeValidatorStrategy(Iterable<AsgValidatorStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region AsgValidatorStrategy Implementation
    @Override
    public ValidationContext apply(AsgQuery query, AsgStrategyContext context) {
        List<ValidationContext> contexts = new ArrayList<>();
        for(AsgValidatorStrategy strategy : this.strategies) {
            try {
                contexts.add(strategy.apply(query, context));
            } catch (Exception ex) {
                int x = 5;
            }
        }

        String[] errors = Stream.ofAll(contexts)
                .filter(validationContext -> !validationContext.valid())
                .flatMap(validationContext -> Stream.of(validationContext.errors()))
                .toJavaArray(String.class);

        return new ValidationContext(errors.length == 0, errors);
    }
    //endregion

    //region Fields
    private Iterable<AsgValidatorStrategy> strategies;
    //endregion
}