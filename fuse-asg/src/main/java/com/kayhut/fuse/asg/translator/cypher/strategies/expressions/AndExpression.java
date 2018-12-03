package com.kayhut.fuse.asg.translator.cypher.strategies.expressions;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.expressions.*;

import java.util.Collection;
import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class AndExpression implements ExpressionStrategies {

    public AndExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<OperatorExpression> operation, Expression expression, AsgQuery query, CypherStrategyContext context) {
        if(expression instanceof And) {
            if(operation.isPresent()) {
                //todo something
            }
            And and = (And) expression;
            context.cypherScope(and);
            Expression lhs = and.lhs();
            strategies.forEach(s->s.apply(Optional.of(and), lhs, query, context));
            Expression rhs = and.rhs();
            strategies.forEach(s->s.apply(Optional.of(and), rhs, query, context));
        }
    }
    private Iterable<ExpressionStrategies> strategies;

}
