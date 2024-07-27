package com.ut.killer.classinfo;

/**
 * Thread root node of TraceCommand
 * @author gongdewei 2020/4/29
 */
public class ThreadNode extends TraceNode {
    private String threadName;
    private long threadId;
    private boolean daemon;
    private int priority;
    private String classloader;

    public ThreadNode() {
        super("thread");
    }

    public ThreadNode(String threadName, long threadId, boolean daemon, int priority, String classloader) {
        super("thread");
        this.threadName = threadName;
        this.threadId = threadId;
        this.daemon = daemon;
        this.priority = priority;
        this.classloader = classloader;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getClassloader() {
        return classloader;
    }

    public void setClassloader(String classloader) {
        this.classloader = classloader;
    }
}
