package com.yangdb.fuse.asg;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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


import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2QueryTransformer;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;

/**
 * Created by liorp on 12/15/2017.
 */
public class AsgGraphQLTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery>  {
    private GraphQL2QueryTransformer graphQL2QueryTransformer;
    private final QueryToAsgTransformer queryTransformer;

    //region Constructors
    @Inject
    public AsgGraphQLTransformer(GraphQL2QueryTransformer graphQL2QueryTransformer,
                                 QueryToAsgTransformer queryTransformer) {
        this.graphQL2QueryTransformer = graphQL2QueryTransformer;
        this.queryTransformer = queryTransformer;
    }
    //endregion

    //region QueryTransformer Implementation

    @Override
    public AsgQuery transform(QueryInfo<String> query) {
        Query transform = graphQL2QueryTransformer.transform(query);
        return queryTransformer.transform(transform);
    }

    //endregion

}
