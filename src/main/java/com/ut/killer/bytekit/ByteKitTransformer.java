package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AgentUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ByteKitTransformer {
    public static byte[] bytekitTransformer(Class<?> className, String[] methodNames) throws Exception {
//        AgentUtils.install();

        // 解析定义的 Interceptor类 和相关的注解
        DefaultInterceptorClassParser interceptorClassParser = new DefaultInterceptorClassParser();
        List<InterceptorProcessor> processors = interceptorClassParser.parse(SampleInterceptor.class);

        System.out.println("begin bytekitTransformer : " + className);
        // 加载字节码
        ClassNode classNode = AsmUtils.loadClass(className);

        // 对加载到的字节码做增强处理
        for (MethodNode methodNode : classNode.methods) {
            System.out.println("methodName : " + methodNode.name);
            if (methodNode.name.equals("queryImages")) {
                MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
                for (InterceptorProcessor interceptor : processors) {
                    interceptor.process(methodProcessor);
                }
                break;
            }
        }
        // 获取增强后的字节码
        byte[] bytes = AsmUtils.toBytes(classNode);
        // 查看反编译结果
//        System.out.println(Decompiler.decompile(bytes));
        // 等待，查看未增强里的输出结果
//        TimeUnit.SECONDS.sleep(10);
        // 通过 reTransform 增强类
        AgentUtils.reTransform(className, bytes);
//        System.in.read();
        System.out.println("after bytekitTransformer : " + className);
        return bytes;
    }
}
