package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.asm.location.Location;
import com.alibaba.bytekit.asm.location.MethodInsnNodeWare;
import com.alibaba.bytekit.utils.AgentUtils;
import com.alibaba.bytekit.utils.AsmOpUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.Opcodes;
import com.alibaba.deps.org.objectweb.asm.Type;
import com.alibaba.deps.org.objectweb.asm.tree.AbstractInsnNode;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodInsnNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;
import com.ut.killer.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ByteKitDemo1 {
    private static final Logger logger = LoggerFactory.getLogger(ByteKitDemo1.class);

    public static void main(String[] args) throws Exception {
        // 启动Sample，不断执行

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1; ++i) {
                    try {
                        Sample sample = new Sample();
                        Thread.sleep(1000);
//                        String result = sample.hello("" + i, (i % 3) == 0);
                        String result = sample.hello("" + i, false);
                        System.out.println("call hello result: " + result);
                    } catch (Throwable e) {
                        // ignore
                        e.printStackTrace();
                        System.out.println("call hello exception: " + e);
                    }
                }
            }
        });
        t.start();

        Class<?> targetClass = Sample.class;
        String className = targetClass.getName();
        boolean skipJDKTrace = true;
        AdviceListener listener = new TraceAdviceListener(true);
        ClassLoader inClassLoader = Thread.currentThread().getContextClassLoader();
        SpyAPI.setSpy(new SpyImpl());


        //keep origin class reader for bytecode optimizations, avoiding JVM metaspace OOM.
        ClassNode classNode = new ClassNode(Opcodes.ASM9);
        // remove JSR https://github.com/alibaba/arthas/issues/1304
        classNode = AsmUtils.removeJSRInstructions(classNode);


        // 解析定义的 Interceptor类 和相关的注解
        DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();

        final List<InterceptorProcessor> interceptorProcessors = new ArrayList<>();

        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor1.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor2.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor3.class));

        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyTraceExcludeJDKInterceptor1.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyTraceExcludeJDKInterceptor2.class));
        interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyTraceExcludeJDKInterceptor3.class));

        System.out.println("targetClass " + targetClass);
        // 加载字节码
//        ClassNode classNode = AsmUtils.loadClass(targetClass);

        List<MethodNode> matchedMethods = new ArrayList<MethodNode>();
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("hello")) {
                matchedMethods.add(methodNode);
            }
        }

        // https://github.com/alibaba/arthas/issues/1690
        if (AsmUtils.isEnhancerByCGLIB(className)) {
            for (MethodNode methodNode : matchedMethods) {
                if (AsmUtils.isConstructor(methodNode)) {
                    AsmUtils.fixConstructorExceptionTable(methodNode);
                }
            }
        }

        for (MethodNode methodNode : matchedMethods) {
            if (AsmUtils.isNative(methodNode)) {
                logger.info("ignore native method: {}",
                        AsmUtils.methodDeclaration(Type.getObjectType(classNode.name), methodNode));
                continue;
            }

            // 先查找是否有 atBeforeInvoke 函数，如果有，则说明已经有trace了，则直接不再尝试增强，直接插入 listener
            if (AsmUtils.containsMethodInsnNode(methodNode, Type.getInternalName(SpyAPI.class), "atBeforeInvoke")) {

                for (AbstractInsnNode insnNode = methodNode.instructions.getFirst(); insnNode != null; insnNode = insnNode
                        .getNext()) {
                    if (insnNode instanceof MethodInsnNode) {
                        final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                        if (skipJDKTrace) {
                            if (methodInsnNode.owner.startsWith("java/")) {
                                continue;
                            }
                        }
                        // 原始类型的box类型相关的都跳过
                        if (AsmOpUtils.isBoxType(Type.getObjectType(methodInsnNode.owner))) {
                            continue;
                        }
                        AdviceListenerManager.registerTraceAdviceListener(inClassLoader, className,
                                methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, listener);
                    }
                }
            } else {
                System.out.println("here ....  " + matchedMethods.size());
                MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
                for (InterceptorProcessor interceptor : interceptorProcessors) {
                    try {
                        List<Location> locations = interceptor.process(methodProcessor);
                        for (Location location : locations) {
                            if (location instanceof MethodInsnNodeWare) {
                                MethodInsnNodeWare methodInsnNodeWare = (MethodInsnNodeWare) location;
                                MethodInsnNode methodInsnNode = methodInsnNodeWare.methodInsnNode();

                                AdviceListenerManager.registerTraceAdviceListener(inClassLoader, className,
                                        methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, listener);
                            }
                        }

                    } catch (Throwable e) {
                        logger.error("enhancer error, class: {}, method: {}, interceptor: {}", classNode.name, methodNode.name, interceptor.getClass().getName(), e);
                    }
                }
            }

            // enter/exist 总是要插入 listener
            AdviceListenerManager.registerAdviceListener(inClassLoader, className, methodNode.name, methodNode.desc,
                    listener);
        }

        // https://github.com/alibaba/arthas/issues/1223 , V1_5 的major version是49
        if (AsmUtils.getMajorVersion(classNode.version) < 49) {
            classNode.version = AsmUtils.setMajorVersion(classNode.version, 49);
        }

//        byte[] enhanceClassByteArray = AsmUtils.toBytes(classNode, inClassLoader, classReader);

        // 增强成功，记录类
//        classBytesCache.put(classBeingRedefined, new Object());

        // dump the class
//        dumpClassIfNecessary(className, enhanceClassByteArray, affect);


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
