package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by Roman on 18/05/2017.
 */
public class CompareQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompareQueryTranslator() {

    }

    public CompareQueryTranslator(boolean shouldAggregateRange) {
        this.shouldAggregateRange = shouldAggregateRange;
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate.getBiPredicate() instanceof Compare)) {
            return queryBuilder;
        }

        Compare compare = (Compare) predicate.getBiPredicate();
        String rangeName = shouldAggregateRange ? key : null;
        switch (compare) {
            case eq:
                queryBuilder.push().term(key, predicate.getValue()).pop();
                break;
            case neq:
                queryBuilder.push().bool().mustNot().term(key, predicate.getValue()).pop();
                break;
            case gt:
                queryBuilder.push().range(rangeName, key).from(predicate.getValue()).includeLower(false).pop();
                break;
            case gte:
                queryBuilder.push().range(rangeName, key).from(predicate.getValue()).includeLower(true).pop();
                break;
            case lt:
                queryBuilder.push().range(rangeName, key).to(predicate.getValue()).includeUpper(false).pop();
                break;
            case lte:
                queryBuilder.push().range(rangeName, key).to(predicate.getValue()).includeUpper(true).pop();
                break;
        }

        return queryBuilder;
    }
    //endregion

    //region Fields
    private boolean shouldAggregateRange;
    //endregion
}