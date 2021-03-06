package com.yangdb.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Created by lior.perry pc on 5/11/2018.
 */
public class KnowledgeReference {
    private final String cReferenceType = "reference";
    private Reference _ref = new Reference();
    private String _id;

    public String getId() {
       return _id;
    }

    public void setId(String value) {
        _id = value;
    }

    public Reference getRef() {
        return _ref;
    }

    public String getReferenceAsElasticJSON() throws JsonProcessingException {
        // Add needed
        _ref.setType(cReferenceType);

        ObjectMapper mapper = new ObjectMapper();
        return KnowledgeJSONMapperSingleton.getInstance().getMapper().writeValueAsString(_ref);
    }


    public void addToBulk(BulkRequestBuilder bulk, TransportClient client) {
        /*ObjectMapper mapper = new ObjectMapper();
        String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
        bulk.add(client.prepareIndex().setIndex(index).setType(KnowledgeRawSchemaSingleton.cIndexType).setId("ref" + String.format(KnowledgeRawSchemaSingleton.getInstance().getSchema().getIdFormat("reference"), _id))
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(mapper.writeValueAsString(_ref), XContentType.JSON);*/
    }
}
