package com.ut.killer.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.URLClassPath;
import ut.killer.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class AgentUtils {
    private static final Logger logger = LoggerFactory.getLogger(AgentUtils.class);

    /**
     * 动态创建JavaAgent的jar包让jvm加载从而取得Instrumentation对象
     */
    public static void start(String mainClassPath, String agentArgs) throws Exception {
        logger.info("agent attach start");
        correctToolsLoadedOrder();
        File agentJar = createJavaAgentJarFile();
        attachAgent(mainClassPath, agentJar.getAbsolutePath(), agentArgs);
        logger.info("agent attach end");
    }

    public static void attachAgent(String mainClassPath, String agentJarPath, String agentArgs) throws Exception {
        logger.info("agentJarPath：{}", agentJarPath);
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            logger.info("descriptorName={}", descriptor.displayName());
            if (descriptor.displayName().equals(mainClassPath)) {
                attachPid(descriptor.id(), agentJarPath, agentArgs);
            }
        }
    }

    private static void attachPid(String targetJvmPid, String agentJarPath, String agentArgs) throws IOException {
        VirtualMachine vmObj = null;
        try {
            vmObj = VirtualMachine.attach(targetJvmPid);
            if (vmObj != null) {
                logger.info("agent path {}", agentJarPath);
                vmObj.loadAgent(agentJarPath, agentArgs);
            }
        } catch (Exception ex) {
            logger.error("attach pid error", ex);
        } finally {
            if (Objects.nonNull(vmObj)) {
                vmObj.detach();
            }
        }
    }

    /**
     * 同时引入了windows和linux环境下的tools包,为了避免tools包加载的不确定性(系统可能会加载到错误的tools包),
     * 对两个tools包的加载顺序根据当前运行环境必要时进行交换,确保系统优先加载的是正确的tools包
     */
    private static void correctToolsLoadedOrder() throws Exception {
        ClassLoader classLoader = AgentUtils.class.getClassLoader();
        Field ucp = URLClassLoader.class.getDeclaredField("ucp");
        ucp.setAccessible(true);
        URLClassPath urlClassPath = (URLClassPath) ucp.get(classLoader);

        Field pathField = urlClassPath.getClass().getDeclaredField("path");
        pathField.setAccessible(true);
        ArrayList<URL> urls = (ArrayList<URL>) pathField.get(urlClassPath);

        Field loadersField = urlClassPath.getClass().getDeclaredField("loaders");
        loadersField.setAccessible(true);

        ArrayList<Object> loaders = (ArrayList<Object>) loadersField.get(urlClassPath);

        int windowsToolsJarIndex = getToolsJarIndex(urls, "tools.jar");
        int linuxToolsJarIndex = getToolsJarIndex(urls, "tools-linux-1.8.0.jar");

        String osNameLowerCase = System.getProperty("os.name").toLowerCase();
        if (osNameLowerCase.contains("windows")) {
            if (linuxToolsJarIndex < windowsToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        }
        if (osNameLowerCase.contains("linux")) {
            if (windowsToolsJarIndex < linuxToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        }
    }

    private static int getToolsJarIndex(ArrayList<URL> urls, String suffix) {
        int linuxToolsJarIndex = Integer.MAX_VALUE;
        for (int index = 0; index < urls.size(); index++) {
            if (urls.get(index).getPath().endsWith(suffix)) {
                linuxToolsJarIndex = index;
            }
        }
        return linuxToolsJarIndex;
    }

    /**
     * 创建并返回一个 Java agent JAR 文件的 {@code File} 对象。
     * 该方法处理与创建 JAR 文件相关的所有步骤，并确保任何遇到的异常都被适当地处理和传播。
     *
     * @return 包含 Java agent 的 JAR 文件的 {@code File} 对象。
     * @throws Exception 如果创建 JAR 文件过程中出现任何错误，则抛出此异常。
     */
    public static File createJavaAgentJarFile() throws Exception {
        File jar = File.createTempFile("agent", ".jar");
        jar.deleteOnExit();
        Manifest manifest = buildeManifest();
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar.toPath()), manifest)) {
            writeClassFile(AgentLauncher.class, jos);
            writeClassFile(ArgsUtils.class, jos);
            writeClassFile(ClassLoaderManager.class, jos);
            writeClassFile(UTKillerConfiguration.class, jos);
            writeClassFile(YamlUtils.class, jos);
            writeClassFile(ClassPool.class, jos);
            writeClassFile(CtClass.class, jos);
        }
        return jar;
    }

    private static Manifest buildeManifest() {
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(new Attributes.Name("Agent-Class"), AgentLauncher.class.getName());
        attrs.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        attrs.put(new Attributes.Name("Can-Redefine-Classes"), "true");
        return manifest;
    }

    private static void writeClassFile(Class<?> clz, JarOutputStream jos) throws Exception {
        String cname = clz.getName();
        JarEntry e = new JarEntry(cname.replace('.', '/') + ".class");
        jos.putNextEntry(e);
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.get(cname);
        jos.write(clazz.toBytecode());
        jos.closeEntry();
    }
}