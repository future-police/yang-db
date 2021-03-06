package com.yangdb.fuse.test.framework.index;

/*-
 * #%L
 * fuse-test-framework
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

/**
 * Created by roman.margolis on 01/01/2018.
 */
public class GlobalElasticEmbeddedNode {

    public static ElasticEmbeddedNode getInstance() throws Exception {
        return getInstance("fuse.test_elastic");
    }

    public static ElasticEmbeddedNode getInstance(String nodeName) throws Exception {
        if (instance == null) {
            instance = new ElasticEmbeddedNode("target/es", 9200, 9300, nodeName);
        }

        return instance;
    }

    private static ElasticEmbeddedNode instance;
}
