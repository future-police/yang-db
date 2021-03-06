package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.yangdb.fuse.asg.strategy.RuleBoostProvider;
import com.yangdb.fuse.model.query.properties.EProp;

public class KnowledgeRuleBoostProvider implements RuleBoostProvider {
    @Override
    public long getBoost(EProp eProp, int ruleIndex) {

        switch (eProp.getCon().getExpr().toString()) {
            case "title":
                return (long) (2* Math.pow (10, (4-ruleIndex)*2));
            case "nicknames":
                return (long) Math.pow (10, (4-ruleIndex)*2);
        }
        return 1;
    }
}
