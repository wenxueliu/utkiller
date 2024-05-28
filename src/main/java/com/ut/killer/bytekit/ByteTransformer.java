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
import com.ut.killer.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

public class ByteTransformer implements ClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ByteTransformer.class);

    private final static Map<Class<?>/* Class */, Object> classBytesCache = new WeakHashMap<Class<?>, Object>();
    private static SpyImpl spyImpl = new SpyImpl();

    static {
        SpyAPI.setSpy(spyImpl);
    }

    private Map<String, Set<String>> methodNames;

    private Set<String> targetClassNames;

    private static Set<String> enhanceClasses = new HashSet<>();

    private final AdviceListener listener = new TraceAdviceListener(true);;


    public ByteTransformer(Set<String> targetClassNames, Map<String, Set<String>> methodNames) {
        this.targetClassNames = targetClassNames;
        this.methodNames = methodNames;
    }

    @Override
    public byte[] transform(ClassLoader inClassLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            logger.info("classname is null {} {}", inClassLoader.getClass().getName(), classBeingRedefined.getName());
            return classfileBuffer;
        }
        String classNameByDot = className.replace('/', '.');
        if (!targetClassNames.contains(classNameByDot)) {
            return classfileBuffer;
        }
        logger.info("classNameByDot: {}", classNameByDot);
        if (enhanceClasses.contains(classNameByDot)) {
            return classfileBuffer;
        }
        enhanceClasses.add(classNameByDot);
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

            //keep origin class reader for bytecode optimizations, avoiding JVM metaspace OOM.
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

            List<MethodNode> matchedMethods = new ArrayList<>();
            for (MethodNode methodNode : classNode.methods) {
                logger.info("methodName: {} {}", methodNode.name, methodNode.signature);
                if (methodNames.get(classNameByDot).contains(methodNode.name)) {
                    logger.info("hit {} {}", classNameByDot, methodNode.name);
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

            logger.info("matchedMethods size {} ", matchedMethods.size());
            for (MethodNode methodNode : matchedMethods) {
                if (AsmUtils.isNative(methodNode)) {
                    logger.info("ignore native method: {}",
                            AsmUtils.methodDeclaration(Type.getObjectType(classNode.name), methodNode));
                    continue;
                }
                // 先查找是否有 atBeforeInvoke 函数，如果有，则说明已经有trace了，则直接不再尝试增强，直接插入 listener
                if (AsmUtils.containsMethodInsnNode(methodNode, Type.getInternalName(SpyAPI.class), "atBeforeInvoke")) {
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
                } else {
                    logger.info("methodProcessor {} {}", classNode.name, methodNode.name);
                    if (methodNode.instructions.size() == 0) {
                        logger.info("{} {} is interface", classNode.name, methodNode.name);
                        // TODO 找到接口的实现类及实现方法
                        continue;
                    }
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

            // 增强成功，记录类
            classBytesCache.put(classBeingRedefined, new Object());

            // dump the class
//            dumpClassIfNecessary(className, enhanceClassByteArray, affect);

            return enhanceClassByteArray;
        } catch (Throwable t) {
            logger.warn("transform loader[{}]:class[{}] failed.", inClassLoader, className, t);
        }
        return null;
    }
}
