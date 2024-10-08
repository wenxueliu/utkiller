package com.ut.killer.classinfo;

import java.util.ArrayList;
import java.util.List;

public abstract class TraceNode {
    protected TraceNode parent;
    protected List<TraceNode> children;

    private String type;

    public TraceNode(String type) {
        this.type = type;
    }

    public void addChild(TraceNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    public void begin() {
    }

    public void end() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TraceNode parent() {
        return parent;
    }

    public void setParent(TraceNode parent) {
        this.parent = parent;
    }

    public List<TraceNode> getChildren() {
        return children;
    }
}
