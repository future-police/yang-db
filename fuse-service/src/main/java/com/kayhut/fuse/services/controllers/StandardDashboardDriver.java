package com.kayhut.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.kayhut.fuse.dispatcher.driver.DashboardDriver;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardDashboardDriver implements DashboardDriver {

    private Client client;

    //region Constructors
    @Inject
    public StandardDashboardDriver(Client client) {
        this.client = client;
    }

    @Override
    public Map graphElementCount() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        final TermsAggregationBuilder aggregation = new TermsAggregationBuilder("graphElementCount",ValueType.STRING);
        aggregation.field("type");
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((StringTerms) response.getAggregations().get("graphElementCount")).getBuckets().stream()
                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
        return elementCount;
    }

    @Override
    public Map graphElementCreated() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        builder.setQuery(boolQuery()
                .should(termQuery("type", "entity"))
                .should(termQuery("type", "relation")));
        final DateHistogramAggregationBuilder aggregation = new DateHistogramAggregationBuilder("graphElementCreatedOverTime");
        aggregation.field("creationTime");
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((StringTerms) response.getAggregations().get("graphElementCreatedOverTime")).getBuckets().stream()
                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
        return elementCount;
    }

    @Override
    public Map graphFieldValuesCount() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        builder.setQuery(boolQuery()
                .should(termQuery("type", "e.value"))
                .should(termQuery("type", "r.value")));
        final TermsAggregationBuilder aggregation = new TermsAggregationBuilder("graphElementCount",ValueType.STRING);
        aggregation.field("fieldId");
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((StringTerms) response.getAggregations().get("graphElementCount")).getBuckets().stream()
                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
        return elementCount;
    }

    //enStridregion
}
