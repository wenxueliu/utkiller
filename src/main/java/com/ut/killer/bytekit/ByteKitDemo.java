package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AgentUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ByteKitDemo {
    public static void main(String[] args) throws Exception {
        // 启动Sample，不断执行
        final Sample sample = new Sample();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 6; ++i) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        String result = sample.hello("" + i, (i % 3) == 0);
                        System.out.println("call hello result: " + result);
                    } catch (Throwable e) {
                        // ignore
                        System.out.println("call hello exception: " + e.getMessage());
                    }
                }
            }
        });
        t.start();

        // 解析定义的 Interceptor类 和相关的注解
        DefaultInterceptorClassParser interceptorClassParser = new DefaultInterceptorClassParser();
        List<InterceptorProcessor> processors = interceptorClassParser.parse(SampleInterceptor.class);

        // 加载字节码
        ClassNode classNode = AsmUtils.loadClass(Sample.class);

        // 对加载到的字节码做增强处理
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("hello")) {
                MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
                for (InterceptorProcessor interceptor : processors) {
                    interceptor.process(methodProcessor);
                }
            }
        }

        // 获取增强后的字节码
        byte[] bytes = AsmUtils.toBytes(classNode);

        // 查看反编译结果
//        System.out.println(Decompiler.decompile(bytes));

        // 等待，查看未增强里的输出结果
//        TimeUnit.SECONDS.sleep(10);

        // 通过 reTransform 增强类
        AgentUtils.reTransform(Sample.class, bytes);
//        System.in.read();
    }
}
