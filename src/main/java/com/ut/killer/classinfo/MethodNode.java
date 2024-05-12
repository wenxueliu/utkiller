package com.ut.killer.classinfo;

import java.util.List;

/**
 * Method call node of TraceCommand
 *
 * @author gongdewei 2020/4/29
 */
public class MethodNode extends TraceNode {
    private String className;
    private String methodName;
    private int lineNumber;

    private List<ArgumentInfo> args;

    private ReturnInfo returnInfo;

    private Boolean isThrow;
    private String throwExp;

    private boolean isMock;

    /**
     * 是否为invoke方法，true为beforeInvoke，false为方法体入口的onBefore
     */
    private boolean isInvoking;

    public MethodNode(String className, String methodName, List<ArgumentInfo> args, int lineNumber, boolean isInvoking) {
        super("method");
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.lineNumber = lineNumber;
        this.isInvoking = isInvoking;
        this.isMock = false;
    }

    public void begin() {
    }

    public void end() {
    }

    public void setArgs(List<ArgumentInfo> args) {
        this.args = args;
    }

    public List<ArgumentInfo> getArgs() {
        return args;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Boolean getThrow() {
        return isThrow;
    }

    public void setThrow(Boolean aThrow) {
        isThrow = aThrow;
    }

    public String getThrowExp() {
        return throwExp;
    }

    public void setThrowExp(String throwExp) {
        this.throwExp = throwExp;
    }

    public boolean isInvoking() {
        return isInvoking;
    }

    public void setInvoking(boolean invoking) {
        isInvoking = invoking;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setReturnInfo(ReturnInfo returnInfo) {
        this.returnInfo = returnInfo;
    }

    public ReturnInfo getReturnInfo() {
        return returnInfo;
    }
}
