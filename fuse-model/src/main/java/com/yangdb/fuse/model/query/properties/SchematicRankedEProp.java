package com.yangdb.fuse.model.query.properties;

/*-
 *
 * SchematicRankedEProp.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicRankedEProp extends SchematicEProp implements RankingProp {

    public SchematicRankedEProp() {}

    public SchematicRankedEProp(int eNum, String pType, String schematicName, Constraint con, long boost) {
        super(eNum, pType, schematicName, con);
        this.boost = boost;
    }

    public SchematicRankedEProp(SchematicEProp schematicEProp, long boost) {
        this(schematicEProp.geteNum(), schematicEProp.getpType(), schematicEProp.getSchematicName(), schematicEProp.getCon(), boost);
    }


    @Override
    public SchematicRankedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicRankedEProp clone(int eNum) {
        SchematicRankedEProp clone = new SchematicRankedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        clone.boost = boost;
        return clone;
    }

    @Override
    public long getBoost() {
        return boost;
    }

    private long boost;
}
