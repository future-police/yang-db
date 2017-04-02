package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.query.ConstraintOp;

/**
 * Created by User on 26/03/2017.
 */
public interface CypherOps {
    static String getOp(ConstraintOp op) {
        switch (op.name()) {
            case "lt":
                return "<";
            case "eq":
                return "=";
            case "le":
                return "<=";
            case "gt":
                return ">";
            case "ge":
                return ">=";
            default:
                return " <> ";
        }
    }
}