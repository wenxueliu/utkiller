package ut.killer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentLauncher {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final String DEFAULT_UTKILLER_HOME
            = new File(AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParentFile()
            .getParentFile()
            .getParent();

    // 全局持有ClassLoader用于隔离sandbox实现
    private static final Map<String, ClassLoader> sandboxClassLoaderMap
            = new ConcurrentHashMap<>();

    /**
     * 启动加载
     *
     * @param featureString 启动参数
     *                      [namespace,prop]
     * @param inst          inst
     */
    public static void premain(String featureString, Instrumentation inst) {
        install(ArgsUtils.toMap(featureString), inst);
    }

    /**
     * 动态加载
     *
     * @param featureString 启动参数
     *                      [namespace,token,ip,port,prop]
     * @param inst          inst
     */
    public static void agentmain(String featureString, Instrumentation inst) {
        System.out.println("start agent");
        install(ArgsUtils.toMap(featureString), inst);
    }

    private static void install(final Map<String, String> featureMap,
                                final Instrumentation inst) {
        String namespace = featureMap.getOrDefault("namespace", "default");
        int port = Integer.parseInt(featureMap.getOrDefault("port", "8888"));
        String utillerHome = featureMap.getOrDefault("utkiller_home", DEFAULT_UTKILLER_HOME);
        try {
            String home = getUtKillerHome(utillerHome);
            // 将Spy注入到BootstrapClassLoader
//            inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(
//                    getSandboxSpyJarPath(home)
//            )));
            // 构造自定义的类加载器，尽量减少Sandbox对现有工程的侵蚀
            final ClassLoader sandboxClassLoader = loadOrDefineClassLoader(
                    namespace,
                    getSandboxCoreJarPath(home)
            );
            Thread bindingThread = new Thread(() -> {
                try {
                    bind(inst, sandboxClassLoader, port);
                } catch (Throwable throwable) {
                    throwable.printStackTrace(System.err);
                }
            });
            bindingThread.setName("utkiller-binding-thread");
            bindingThread.start();
            bindingThread.join();
        } catch (Throwable cause) {
            throw new RuntimeException("utkiller attach failed.", cause);
        }
    }

    private static void bind(Instrumentation inst, ClassLoader sandboxClassLoader, int port) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        sandboxClassLoader.loadClass("fi.iki.elonen.NanoHTTPD");
        Class<?> httpAgentServer = sandboxClassLoader.loadClass("com.ut.killer.http.HttpAgentServer");
        httpAgentServer.getMethod("begin", Integer.class, Instrumentation.class).invoke(null, port, inst);
    }

    private static boolean isBlankString(final String string) {
        return !isNotBlankString(string);
    }

    private static boolean isNotBlankString(final String string) {
        return Objects.nonNull(string)
                && !string.isEmpty()
                && !string.matches("^\\s*$");
    }

    public static synchronized ClassLoader loadOrDefineClassLoader(final String namespace,
                                                                   final String coreJar) throws Throwable {
        final ClassLoader classLoader;
        // 如果已经被启动则返回之前启动的ClassLoader
        if (sandboxClassLoaderMap.containsKey(namespace)
                && Objects.nonNull(sandboxClassLoaderMap.get(namespace))) {
            classLoader = sandboxClassLoaderMap.get(namespace);
        }
        // 如果未启动则重新加载
        else {
            // classLoader = new SandboxClassLoader(namespace, coreJar);
            classLoader = Thread.currentThread().getContextClassLoader();
            sandboxClassLoaderMap.put(namespace, classLoader);
        }

        return classLoader;
    }

    public static ClassLoader getClassLoader(String namespace) {
        return sandboxClassLoaderMap.get(namespace);
    }

    private static String getUtKillerHome(String home) {
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

    private static String getSandboxCoreJarPath(String home) {
        return home + File.separator + "utkiller-core.jar";
    }
}
