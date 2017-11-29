package com.kayhut.fuse.services.dispatcher.context.processor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

import java.io.IOException;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by Roman on 04/04/2017.
 */
public class QueryCursorPageTestProcessor implements
        QueryCreationOperationContext.Processor,
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor {
    //region Constructors
    @Inject
    public QueryCursorPageTestProcessor(EventBus eventBus, ResourceStore resourceStore, CursorFactory cursorFactory) {
        this.eventBus = eventBus;
        this.resourceStore = resourceStore;
        this.cursorFactory = cursorFactory;
        this.eventBus.register(this);
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if (context.getAsgQuery() != null && context.getExecutionPlan() == null) {
            context = context.of(new PlanWithCost<>(new Plan(), new PlanDetailedCost()));
            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() == null) {
            Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(context.getQueryId());
            if (!queryResource.isPresent()) {
                return context;
            }

            context = context.of(
                    cursorFactory.createCursor(
                            new CursorFactory.Context.Impl(
                                    queryResource.get(),
                                    CreateCursorRequest.CursorType.paths)));

            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) throws IOException {
        if (context.getPageResource() == null) {
            Optional<CursorResource> cursorResource = this.resourceStore.getCursorResource(context.getQueryId(), context.getCursorId());
            if (!cursorResource.isPresent()) {
                return context;
            }

            QueryResult queryResult = cursorResource.get().getCursor().getNextResults(context.getPageSize());
            context = context.of(new PageResource(context.getPageId(), queryResult, context.getPageSize(),0));
            submit(eventBus, context);
        }

        return context;
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private ResourceStore resourceStore;
    private CursorFactory cursorFactory;
    //endregion
}
