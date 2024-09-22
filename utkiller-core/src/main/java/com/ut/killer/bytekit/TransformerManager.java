package com.ut.killer.bytekit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class TransformerManager {
    private Instrumentation instrumentation;

    private ClassFileTransformer transformer;

    private static TransformerManager transformerManager;

    /**
     * 获取 {@link TransformerManager} 的单例实例。
     *
     * @param instrumentation 引导类加载器级别的工具，用于操作目标 JVM 中的类字节码。
     * @return TransformerManager 的实例。
     */
    public static TransformerManager getInstance(Instrumentation instrumentation) {
        if (transformerManager == null) {
            transformerManager = new TransformerManager(instrumentation);
        }
        return transformerManager;
    }

    /**
     * 构造一个新的 {@link TransformerManager} 实例。
     *
     * @param instrumentation 引导类加载器级别的工具，用于操作目标 JVM 中的类字节码。
     */
    public TransformerManager(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    /**
     * 添加一个类文件转换器到管理器中。
     *
     * @param transformer 要添加的 {@link ClassFileTransformer} 实例。
     */
    public void addTransformer(ClassFileTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * 移除一个类文件转换器。
     * <p>
     * 从当前管理的类文件转换器列表中移除最后一个添加的转换器。
     * </p>
     */
    public void removeTransformer() {
        this.instrumentation.removeTransformer(transformer);
    }

    /**
     * 销毁资源。
     * <p>
     * 该方法用于清理在对象销毁前需要释放的所有资源，例如关闭打开的文件句柄，
     * 取消对外部服务的订阅等。确保任何在构造函数中获取的资源在此处得到妥善处理。
     * </p>
     */
    public void destroy() {
        this.instrumentation.removeTransformer(transformer);
    }
}