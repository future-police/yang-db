package com.kayhut.fuse.assembly.knowledge.logical.cursor.modelAdders;

import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.assembly.knowledge.logical.model.LogicalItemBase;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;

public class LogicalModelAdderProvider {
    public LogicalModelAdderProvider() {
        this.logicalModelAdders = new HashMap<>();
        logicalModelAdders.put(String.format("%s.%s", ETypes.LOGICAL_ENTITY, ETypes.ENTITY),
                new LogicalModelEntityPovAdder());
        logicalModelAdders.put(String.format("%s.%s", ETypes.ENTITY, ETypes.REFERENCE),
                new LogicalModelPovReferenceAdder());
    }


    public Map<String, LogicalModelAdder> getLogicalModelAdders() {
        return logicalModelAdders;
    }

    public void setLogicalModelAdders(Map<String, LogicalModelAdder> logicalModelAdders) {
        this.logicalModelAdders = logicalModelAdders;
    }

    public void addChild(LogicalItemBase parentItem, LogicalItemBase childItem, String parentType, String childType) {
        String logicalAdderKey = String.format("%s.%s", parentType, childType);
        LogicalModelAdder logicalAdder = this.logicalModelAdders.get(logicalAdderKey);
        logicalAdder.addChild(parentItem, childItem);
    }


    private Map<String, LogicalModelAdder> logicalModelAdders;


}