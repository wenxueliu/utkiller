package com.ut.killer.classinfo;

public class TraceEntity {

    public TraceTree tree;
    public int deep;

    public TraceEntity(ClassLoader loader) {
        this.tree = createTraceTree(loader);
        this.deep = 0;
    }

    private TraceTree createTraceTree(ClassLoader loader) {
        return new TraceTree(ThreadUtil.getThreadNode(loader, Thread.currentThread()));
    }

    public TraceModel getModel() {
        tree.trim();
        return new TraceModel(tree.getRoot(), tree.getNodeCount());
    }
}
