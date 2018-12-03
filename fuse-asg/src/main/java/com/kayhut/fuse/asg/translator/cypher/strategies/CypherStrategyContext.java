package com.kayhut.fuse.asg.translator.cypher.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.util.ASTNode;

import java.util.Stack;

public class CypherStrategyContext {

    public CypherStrategyContext(Statement statement, AsgEBase<? extends EBase> scope) {
        this.statement = statement;
        this.scope = scope;
        this.cypherScope = new Stack<>();
    }

    public AsgEBase<? extends EBase> getScope() {
        return scope;
    }

    public Statement getStatement() {
        return statement;
    }

    public CypherStrategyContext scope(AsgEBase<? extends EBase> scope) {
        this.scope = scope;
        return this;
    }

    public CypherStrategyContext pushCyScope(ASTNode cypherScope) {
        this.cypherScope.push(cypherScope);
        return this;
    }

    public CypherStrategyContext popCyScope() {
        this.cypherScope.pop();
        return this;
    }

    public ASTNode getCyScope() {
        return cypherScope.peek();
    }

    //region Fields
    private Statement statement;
    private AsgEBase<? extends EBase> scope;
    private Stack<ASTNode> cypherScope;
    //endregion
}
