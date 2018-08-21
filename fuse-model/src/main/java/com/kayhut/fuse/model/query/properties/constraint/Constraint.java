package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "ParameterizedConstraint", value = ParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {

    //region Ctrs
    public Constraint() {
    }

    public Constraint(ConstraintOp op, Object expr) {
        this.op = op;
        this.expr = expr;
    }

    public Constraint(ConstraintOp op, Object expr, String iType) {
        this.op = op;
        this.expr = expr;
        this.iType = iType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Constraint other = (Constraint) o;

        if (this.op == null) {
            if (other.op != null) {
                return false;
            }
        } else {
            if (!this.op.equals(other.op)) {
                return false;
            }
        }

        if (this.expr == null) {
            if (other.expr != null) {
                return false;
            }
        } else {
            if (!this.expr.equals(other.expr)) {
                return false;
            }
        }

        if (this.iType == null) {
            if (other.iType != null) {
                return false;
            }
        } else {
            if (!this.iType.equals(other.iType)) {
                return false;
            }
        }

        return true;
    }
    //endregion

    //region Properties
    public ConstraintOp getOp() {
        return op;
    }

    public void setOp(ConstraintOp op) {
        this.op = op;
    }

    public Object getExpr() {
        return expr;
    }

    public void setExpr(Object expr) {
        this.expr = expr;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType;
    }
    //endregion

    //region Fields
    private ConstraintOp op;
    private Object expr;
    //default - inclusive
    private String iType = "[]";
    //endregion

    public static Constraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp) {
        return of(op, exp, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp, String iType) {
        Constraint constraint = new Constraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }

}
