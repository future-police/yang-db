package com.yangdb.fuse.unipop.controller.promise;

/*-
 * #%L
 * fuse-dv-unipop
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
 * Created by lior.perry on 19/03/2017.
 */
public class GlobalConstants {
    public static class HasKeys {
        public static final String PROMISE = "promise";
        public static final String CONSTRAINT = "constraint";
        public static final String DIRECTION = "direction";
        public static final String COUNT = "count";
    }

    public static class Labels {
        public static final String PROMISE = "promise";
        public static final String PROMISE_FILTER = "promiseFilter";
        public static final String NONE = "_none_";
    }

    public static class EdgeSchema {
        public static String SOURCE_ID = "entityA.id";
        public static String DEST_ID = "entityB.id";
        public static String SOURCE = "entityA";
        public static String DEST = "entityB";
    }
}
