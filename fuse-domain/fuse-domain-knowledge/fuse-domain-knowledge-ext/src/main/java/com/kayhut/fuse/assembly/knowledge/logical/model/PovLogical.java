package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.ArrayList;
import java.util.List;

public class PovLogical extends LogicalItemBase {
    public PovLogical(String context, String category, Metadata metadata) {
        super(metadata);
        this.context = context;
        this.category = category;
    }

    //region Properties
    public List<ReferenceLogical> getReferences() {
        return references;
    }

    public void setReferences(List<ReferenceLogical> references) {
        this.references = references;
    }
    //endregion

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    //region Fields
    private String context;
    private String category;
    private List<ReferenceLogical> references= new ArrayList<>();
    //endregion
}