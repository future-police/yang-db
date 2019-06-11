package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

public class RvalueLong extends RvalueBase {
    //region Constructors
    public RvalueLong() {
        super();
    }

    public RvalueLong(String relId, String context, String fieldId, long longValue) {
        this(relId, context, fieldId, longValue, null);
    }

    public RvalueLong(String relId, String context, String fieldId, long longValue, Metadata metadata) {
        super(relId, context, fieldId, metadata);
        this.longValue = longValue;
    }
    //endregion

    //region Properties
    public float getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }
    //endregion

    //region Fields
    private long longValue;
    //endregion
}
