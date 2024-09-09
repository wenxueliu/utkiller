package com.ut.killer.bytekit;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.asm.location.Location;
import com.alibaba.bytekit.asm.location.MethodInsnNodeWare;
import com.alibaba.bytekit.utils.AsmOpUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.deps.org.objectweb.asm.ClassReader;
import com.alibaba.deps.org.objectweb.asm.Opcodes;
import com.alibaba.deps.org.objectweb.asm.Type;
import com.alibaba.deps.org.objectweb.asm.tree.AbstractInsnNode;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodInsnNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;
import com.ut.killer.EnhanceManager;
import com.ut.killer.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.killer.SpyAPI;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

public class ByteTransformer implements ClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ByteTransformer.class);

    private static SpyImpl spyImpl = new SpyImpl();

    static {
        SpyAPI.setSpy(spyImpl);
    }

    private Map<String, Set<String>> class2Methods;

    private Set<String> targetClassNames;

    private final AdviceListener listener = new TraceAdviceListener(true);
    ;


    public ByteTransformer(Set<String> targetClassNames, Map<String, Set<String>> class2Methods) {
        this.targetClassNames = targetClassNames;
        this.class2Methods = class2Methods;
    }

    @Override
    public byte[] transform(ClassLoader inClassLoader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) {
            logger.info("classname is null {} {}", inClassLoader.getClass().getName(), classBeingRedefined.getName());
            return classfileBuffer;
        }
        String classNameByDot = className.replace('/', '.');
        // 忽略掉不需要增强的类
        if (!targetClassNames.contains(classNameByDot)) {
            return classfileBuffer;
        }
        logger.info("classNameByDot: {}", classNameByDot);
        // 避免重复增强
        if (EnhanceManager.contains(classNameByDot)) {
            return classfileBuffer;
        }
        logger.info("enhance {} {}", inClassLoader, classNameByDot);
        try {
            // 检查classloader能否加载到 SpyAPI，如果不能，则放弃增强
            try {
                if (inClassLoader != null) {
                    inClassLoader.loadClass(SpyAPI.class.getName());
                }
            } catch (Throwable e) {
                logger.error("the classloader can not load SpyAPI, ignore it. classloader: {}, className: {}",
                        inClassLoader.getClass().getName(), className, e);
                return null;
            }

            // keep origin class reader for bytecode optimizations, avoiding JVM metaspace OOM.
            // https://github.com/alibaba/arthas/issues/1304
            ClassNode classNode = new ClassNode(Opcodes.ASM9);
            ClassReader classReader = AsmUtils.toClassNode(classfileBuffer, classNode);
            // remove JSR https://github.com/alibaba/arthas/issues/1304
            classNode = AsmUtils.removeJSRInstructions(classNode);

            // 生成增强字节码
            DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();

            final List<InterceptorProcessor> interceptorProcessors = new ArrayList<>();

            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor1.class));
            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor2.class));
            interceptorProcessors.addAll(defaultInterceptorClassParser.parse(SpyInterceptors.SpyInterceptor3.class));

            List<MethodNode> todoEnhanceMethodNods = new ArrayList<>();
            for (MethodNode methodNode : classNode.methods) {
                logger.info("methodName: {} {}", methodNode.name, methodNode.signature);
                if (class2Methods.get(classNameByDot).contains(methodNode.name)) {
                    logger.info("hit {} {}", classNameByDot, methodNode.name);
                    todoEnhanceMethodNods.add(methodNode);
                }
            }

            // https://github.com/alibaba/arthas/issues/1690
            if (AsmUtils.isEnhancerByCGLIB(className)) {
                for (MethodNode methodNode : todoEnhanceMethodNods) {
                    if (AsmUtils.isConstructor(methodNode)) {
                        AsmUtils.fixConstructorExceptionTable(methodNode);
                    }
                }
            }

            logger.info("matchedMethods size {} ", todoEnhanceMethodNods.size());
            for (MethodNode methodNode : todoEnhanceMethodNods) {
                if (AsmUtils.isNative(methodNode)) {
                    logger.info("ignore native method: {}",
                            AsmUtils.methodDeclaration(Type.getObjectType(classNode.name), methodNode));
                    continue;
                }
                // 先查找是否有 atBeforeInvoke 函数，如果有，则说明已经有trace了，则直接不再尝试增强，直接插入 listener
                if (AsmUtils.containsMethodInsnNode(methodNode, Type.getInternalName(SpyAPI.class), "atBeforeInvoke")) {
                    handleMethodWithTrace(inClassLoader, className, methodNode, classNode);
                } else {
                    logger.info("methodProcessor {} {}", classNode.name, methodNode.name);
                    if (methodNode.instructions.size() == 0) {
                        logger.info("{} {} is interface", classNode.name, methodNode.name);
                        // TODO 找到接口的实现类及实现方法
                        continue;
                    }
                    handleMethodWithoutTrace(inClassLoader, className, methodNode, classNode, interceptorProcessors);
                }

                logger.info("methodNode {} {}", methodNode.name, methodNode.desc);
                // enter/exist 总是要插入 listener
                AdviceListenerManager.registerAdviceListener(inClassLoader, className, methodNode.name, methodNode.desc,
                        listener);
            }

            // https://github.com/alibaba/arthas/issues/1223 , V1_5 的major version是49
            if (AsmUtils.getMajorVersion(classNode.version) < 49) {
                classNode.version = AsmUtils.setMajorVersion(classNode.version, 49);
            }

            byte[] enhanceClassByteArray = AsmUtils.toBytes(classNode, inClassLoader, classReader);

            EnhanceManager.put(className, classfileBuffer.clone());
            return enhanceClassByteArray;
        } catch (Throwable t) {
            logger.error("transform loader[{}]:class[{}] failed.", inClassLoader, className, t);
        }
        return null;
    }

    private void handleMethodWithoutTrace(ClassLoader inClassLoader, String className, MethodNode methodNode, ClassNode classNode, List<InterceptorProcessor> interceptorProcessors) {
        MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
        for (InterceptorProcessor interceptor : interceptorProcessors) {
            try {
                List<Location> locations = interceptor.process(methodProcessor);
                logger.info("locations {}", locations);
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

    private void handleMethodWithTrace(ClassLoader inClassLoader, String className, MethodNode methodNode, ClassNode classNode) {
        logger.info("hasMethod atBeforeInvoke {} {}", classNode.name, methodNode.name);
        for (AbstractInsnNode insnNode = methodNode.instructions.getFirst(); insnNode != null; insnNode = insnNode
                .getNext()) {
            logger.info("iterator {} {} {}", classNode.name, methodNode.name, insnNode.getType());
            if (insnNode instanceof MethodInsnNode) {
                final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                if (methodInsnNode.owner.startsWith("java/")) {
                    continue;
                }
                // 原始类型的box类型相关的都跳过
                if (AsmOpUtils.isBoxType(Type.getObjectType(methodInsnNode.owner))) {
                    continue;
                }
                AdviceListenerManager.registerTraceAdviceListener(inClassLoader, className,
                        methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, listener);
            }
        }
    }
}
