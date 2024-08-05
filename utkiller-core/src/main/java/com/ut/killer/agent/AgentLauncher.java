package com.ut.killer.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentLauncher {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final String DEFAULT_UTKILLER_BASE
            = new File(AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParentFile()
            .getParentFile()
            .getParent();

    // 全局持有ClassLoader用于隔离sandbox实现
    private static final Map<String, AgentClassLoader> namespace2ClassLoader
            = new ConcurrentHashMap<>();


    public static void premain(String featureString, Instrumentation inst) {
        System.out.println("start premain begin");
        install(ArgsUtils.toMap(featureString), inst);
        System.out.println("start agentmain end");
    }

    public static void agentmain(String featureString, Instrumentation inst) {
        System.out.println("start agentmain begin");
        install(ArgsUtils.toMap(featureString), inst);
        System.out.println("start agentmain end");
    }

    public static void install(final Map<String, String> featureMap,
                               final Instrumentation inst) {
        String namespace = featureMap.getOrDefault("namespace", "default");
        int port = Integer.parseInt(featureMap.getOrDefault("port", "8888"));
        try {
            String basePath = getBasePath(featureMap);
            // 将Spy注入到BootstrapClassLoader
            loadSpy(inst, getSpyJarPath(basePath));
            System.out.println(basePath);
            // 构造自定义的类加载器，尽量减少Sandbox对现有工程的侵蚀
            final ClassLoader agentClassLoader = loadOrDefineClassLoader(namespace, getCoreJarPath(basePath));

            handler(inst, agentClassLoader, port);
        } catch (Throwable cause) {
            throw new RuntimeException("utkiller attach failed.", cause);
        }
    }

    private static void loadSpy(Instrumentation inst, String spyPath) throws Throwable {
        // 将Spy添加到BootstrapClassLoader
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        Class<?> spyClass = null;
        if (parent != null) {
            try {
                spyClass = parent.loadClass("ut.killer.command.SpyAPI");
            } catch (Throwable e) {
                // ignore
            }
        }
        if (spyClass == null) {
            File spyJarFile = new File(spyPath);
            inst.appendToBootstrapClassLoaderSearch(new JarFile(spyJarFile));
        } else {
            throw new IllegalStateException("can not find " + spyPath);
        }
    }

    public static synchronized ClassLoader loadOrDefineClassLoader(final String namespace,
                                                                   final String coreJar) throws Throwable {

        final AgentClassLoader agentClassLoader;

        // 如果已经被启动则返回之前启动的ClassLoader
        if (namespace2ClassLoader.containsKey(namespace)
                && Objects.nonNull(namespace2ClassLoader.get(namespace))) {
            agentClassLoader = namespace2ClassLoader.get(namespace);
        }

        // 如果未启动则重新加载
        else {
            agentClassLoader = new AgentClassLoader(namespace, coreJar);
            namespace2ClassLoader.put(namespace, agentClassLoader);
        }

        return agentClassLoader;
    }

    private static String getBasePath(final Map<String, String> featureMap) {
        String home = featureMap.getOrDefault("utkiller_base", DEFAULT_UTKILLER_BASE);
        if (isWindows()) {
            Matcher m = Pattern.compile("(?i)^[/\\\\]([a-z])[/\\\\]").matcher(home);
            if (m.find()) {
                home = m.replaceFirst("$1:/");
            }
        }
        return home;
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }

    protected static String getCoreJarPath(String basePath) {
        return basePath + File.separatorChar + "utkiller-core" + File.separator + "target" + File.separator + "utkiller-core-1.0.0-SNAPSHOT.jar";
    }

    protected static String getSpyJarPath(String basePath) {
        return basePath + File.separatorChar + "utkiller-spy" + File.separator + "target" + File.separator + "utkiller-spy-1.0.0-SNAPSHOT.jar";
    }

    protected static void handler(Instrumentation inst, ClassLoader agentClassLoader, int port) throws
            ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        agentClassLoader.loadClass("fi.iki.elonen.NanoHTTPD");
        Class<?> httpAgentServer = agentClassLoader.loadClass("com.ut.killer.http.HttpAgentServer");
        httpAgentServer.getMethod("begin", Integer.class, Instrumentation.class).invoke(null, port, inst);
    }
}
