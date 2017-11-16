package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;

/**
 * Created by User on 27/03/2017.
 */
public class ElementConstraintSearchAppender extends SearchQueryAppenderBase<PromiseElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, PromiseElementControllerContext promiseElementControllerContext) {
        if (!promiseElementControllerContext.getConstraint().isPresent()) {
            return false;
        }

        new TraversalQueryTranslator(queryBuilder.seekRoot().query().filtered().filter().bool().must(), false)
                .visit(promiseElementControllerContext.getConstraint().get().getTraversal());

        return true;
    }
    //endregion
}