package com.kayhut.fuse.services;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

/**
 * Created by lior on 19/02/2017.
 */
@Singleton
public class SimpleSearchController implements SearchController {
    private EventBus eventBus;

    @Inject
    public SimpleSearchController(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public ContentResponse search(CreateQueryRequest request) {
        /*String id = getOrCreateId(request.getId());
        ContentResponse response = ContentResponse.Builder.builder(id)
                .queryMetadata(new QueryMetadata(id, request.getName(), request.getType(), System.currentTimeMillis()))
                //todo implement this
                .queryResourceResult(new QueryResourceInfo())
                .data(GraphContent.GraphBuilder.builder(request.getId())
                        .data(new QueryResult())
                        .build())
                .build();
        //publish execution isCompleted
        eventBus.post(new ExecutionCompleteCommand(response));
        return response;*/
        return null;
    }
}
