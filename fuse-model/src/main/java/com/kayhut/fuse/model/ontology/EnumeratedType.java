package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EnumeratedType {

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    @Override
    public String toString()
    {
        return "EnumeratedType [values = "+values+", eType = "+eType+"]";
    }

    //region Fields
    private String eType;
    private List<Value> values;
    //endregion
}
