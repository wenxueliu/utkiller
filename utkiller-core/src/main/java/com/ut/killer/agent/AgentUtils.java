package com.ut.killer.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.URLClassPath;
import ut.killer.AgentLauncher;
import ut.killer.ArgsUtils;
import ut.killer.UTKillerConfiguration;
import ut.killer.YamlUtils;

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
        String configPath = ArgsUtils.toMap(agentArgs).getOrDefault("configPath", "");
        UTKillerConfiguration config = YamlUtils.parse(configPath);
        String baseDir = config.getBaseDir();
        File agentJar = AgentUtils.createJavaAgentJarFile(baseDir);
        attachAgent(mainClassPath, agentJar.getAbsolutePath(), agentArgs);
        logger.info("agent attach end");
    }

    public static void attachAgent(final String mainClassPath,
                                    final String agentJarPath,
                                    final String agentArgs) throws Exception {
        logger.info("agentJarPath：{}", agentJarPath);
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            System.out.println(descriptor.displayName());
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
                System.out.println(agentJarPath);
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
        Field loadersField = urlClassPath.getClass().getDeclaredField("loaders");
        pathField.setAccessible(true);
        loadersField.setAccessible(true);
        ArrayList<URL> urls = (ArrayList<URL>) pathField.get(urlClassPath);
        ArrayList<Object> loaders = (ArrayList<Object>) loadersField.get(urlClassPath);
        int windowsToolsJarIndex = Integer.MAX_VALUE;
        int linuxToolsJarIndex = Integer.MAX_VALUE;
        for (int i = 0; i < urls.size(); i++) {
            if (urls.get(i).getPath().endsWith("tools.jar")) {
                windowsToolsJarIndex = i;
            } else if (urls.get(i).getPath().endsWith("tools-linux-1.8.0.jar")) {
                linuxToolsJarIndex = i;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (linuxToolsJarIndex < windowsToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            if (windowsToolsJarIndex < linuxToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        }
    }

    public static File createJavaAgentJarFile(String baseDir) throws Exception {
        return new File(baseDir + File.separator + "utkiller-agent.jar");
    }

    public static File createJavaAgentJarFile2() throws Exception {
        File jar = File.createTempFile("agent", ".jar");
        jar.deleteOnExit();
        Manifest manifest = buildeManifest();
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar.toPath()), manifest)) {
            writeClassFile(AgentLauncher.class, jos);
            writeClassFile(ArgsUtils.class, jos);
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