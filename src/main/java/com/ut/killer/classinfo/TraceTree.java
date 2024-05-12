package com.ut.killer.classinfo;

import com.ut.killer.command.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Tree model of TraceCommand
 *
 * @author gongdewei 2020/4/28
 */
public class TraceTree {
    private static final Logger logger = LoggerFactory.getLogger(TraceTree.class);

    private TraceNode root;

    private TraceNode current;
    private int nodeCount = 0;

    public TraceTree(ThreadNode root) {
        this.root = root;
        this.current = root;
    }

    /**
     * 开始一个新的跟踪节点。
     * 当进入一个新的方法或行时调用此方法，用于记录代码执行的路径。
     *
     * @param advice 包含当前执行方法信息的Advice对象，用于获取类名和方法名。
     * @param lineNumber 当前执行的代码行号。
     * @param isInvoking 表示当前方法是否是被调用的状态。
     */
    public void begin(Advice advice, int lineNumber, boolean isInvoking) {
        String className = advice.getClazz().getName();
        String methodName = advice.getMethod().getName();
        logger.info("begin className {}", className);

        TraceNode child = findChild(current, className, methodName, lineNumber);
        if (child == null) {
            child = new MethodNode(className, methodName, advice.getParams(), lineNumber, isInvoking);
            current.addChild(child);
        }
        if (!isSameClassWithRoot(className)) {
            logger.info("{} is not equal root className", className);
            MethodNode methodNode = (MethodNode) child;
            methodNode.setMock(true);
        }
        child.begin();
        child.parent = current;
        current = child;
        nodeCount += 1;
    }

    private boolean isSameClassWithRoot(String className) {
        for (TraceNode child : root.getChildren()) {
            if (child.getType().equals("method")) {
                MethodNode methodNode = (MethodNode) child;
                logger.info("{} isSameClassWithRoot {}", methodNode.getClassName(), className);
                if (methodNode.getClassName().equals(className)) {
                    return true;
                }
            }
        }
        return false;
    }

    private TraceNode findChild(TraceNode node, String className, String methodName, int lineNumber) {
        List<TraceNode> childList = node.getChildren();
        if (childList != null) {
            //less memory than foreach/iterator
            for (int i = 0; i < childList.size(); i++) {
                TraceNode child = childList.get(i);
                if (matchNode(child, className, methodName, lineNumber)) {
                    return child;
                }
            }
        }
        return null;
    }

    private boolean matchNode(TraceNode node, String className, String methodName, int lineNumber) {
        if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
//            if (lineNumber != methodNode.getLineNumber()) return false;
            if (className != null ? !className.equals(methodNode.getClassName()) : methodNode.getClassName() != null)
                return false;
            return methodName != null ? methodName.equals(methodNode.getMethodName()) : methodNode.getMethodName() == null;
        }
        return false;
    }

    public ReturnInfo getReturnInfo(Object object) {
        if (Objects.isNull(object)) {
            ReturnInfo returnInfo = new ReturnInfo();
            returnInfo.setValue(null);
            returnInfo.setType("Void");
            return returnInfo;
        }
        ReturnInfo returnInfo = new ReturnInfo();
        returnInfo.setType(object.getClass().getTypeName());
        returnInfo.setValue(object);
        return returnInfo;
    }

    public void end(Advice advice, int lineNumber) {
        String className = advice.getClazz().getName();
        String methodName = advice.getMethod().getName();
        logger.info("begin {} {} {}", className, methodName, advice.getReturnObj());

        TraceNode child = findChild(current, className, methodName, lineNumber);
        if (child == null) {
            logger.info("child is null");
            return;
        }
        MethodNode methodNode = (MethodNode) child;
        methodNode.setReturnInfo(getReturnInfo(advice.getReturnObj()));

        current.end();
        if (current.parent() != null) {
            current = current.parent();
        }
    }

    public void end(Advice advice, Throwable throwable, int lineNumber) {
        ThrowNode throwNode = new ThrowNode();
        throwNode.setException(throwable.getClass().getName());
        throwNode.setMessage(throwable.getMessage());
        throwNode.setLineNumber(lineNumber);
        current.addChild(throwNode);
        this.end(advice, true);
    }

    public void end(Advice advice, boolean isThrow) {
        if (isThrow) {
            if (current instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) current;
                methodNode.setThrow(true);
            }
        }
        this.end(advice, -1);
    }

    /**
     * 修整树结点
     */
    public void trim() {
        this.normalizeClassName(root);
    }

    /**
     * 转换标准类名，放在trace结束后统一转换，减少重复操作
     *
     * @param node
     */
    private void normalizeClassName(TraceNode node) {
        if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            String nodeClassName = methodNode.getClassName();
            String normalizeClassName = StringUtils.normalizeClassName(nodeClassName);
            methodNode.setClassName(normalizeClassName);
        }
        List<TraceNode> children = node.getChildren();
        if (children != null) {
            //less memory fragment than foreach
            for (int i = 0; i < children.size(); i++) {
                TraceNode child = children.get(i);
                normalizeClassName(child);
            }
        }
    }

    public TraceNode getRoot() {
        return root;
    }

    public TraceNode current() {
        return current;
    }

    public int getNodeCount() {
        return nodeCount;
    }
}
