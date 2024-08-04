package ut.killer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
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
    private static final Map<String/*NAMESPACE*/, SandboxClassLoader> sandboxClassLoaderMap
            = new ConcurrentHashMap<>();

    private static String getSandboxSpyJarPath(String home) {
        return home + File.separatorChar + "utkiller-spy" + File.separator + "target" + File.separator + "utkiller-spy-1.0.0-SNAPSHOT.jar";
    }

    /**
     * 启动加载
     *
     * @param featureString 启动参数
     *                      [namespace,prop]
     * @param inst          inst
     */
    public static void premain(String featureString, Instrumentation inst) {
        install(toFeatureMap(featureString), inst);
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
        install(toFeatureMap(featureString), inst);
    }

    private static void install(final Map<String, String> featureMap,
                                final Instrumentation inst) {
        String namespace = featureMap.getOrDefault("namespace", "default");
        int port = Integer.parseInt(featureMap.getOrDefault("port", "8888"));
        try {
            String home = getUtKillerHome(featureMap);
            // 将Spy注入到BootstrapClassLoader
            inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(
                    getSandboxSpyJarPath(home)
            )));
            System.out.println(home);
            // 构造自定义的类加载器，尽量减少Sandbox对现有工程的侵蚀
            final ClassLoader sandboxClassLoader = loadOrDefineClassLoader(
                    namespace,
                    getSandboxCoreJarPath(home)
            );

            sandboxClassLoader.loadClass("fi.iki.elonen.NanoHTTPD");
            Class<?> httpAgentServer = sandboxClassLoader.loadClass("com.ut.killer.http.HttpAgentServer");
            httpAgentServer.getMethod("begin", Integer.class, Instrumentation.class).invoke(null, port, inst);
        } catch (Throwable cause) {
            throw new RuntimeException("utkiller attach failed.", cause);
        }
    }

    private static Map<String, String> toFeatureMap(final String featureString) {
        final Map<String, String> featureMap = new LinkedHashMap<>();

        // 不对空字符串进行解析
        if (isBlankString(featureString)) {
            return featureMap;
        }

        // KV对片段数组
        final String[] kvPairSegmentArray = featureString.split(";");
        if (kvPairSegmentArray.length == 0) {
            return featureMap;
        }

        for (String kvPairSegmentString : kvPairSegmentArray) {
            if (isBlankString(kvPairSegmentString)) {
                continue;
            }
            final String[] kvSegmentArray = kvPairSegmentString.split("=");
            if (kvSegmentArray.length != 2
                    || isBlankString(kvSegmentArray[0])
                    || isBlankString(kvSegmentArray[1])) {
                continue;
            }
            featureMap.put(kvSegmentArray[0], kvSegmentArray[1]);
        }

        return featureMap;
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

        final SandboxClassLoader classLoader;

        // 如果已经被启动则返回之前启动的ClassLoader
        if (sandboxClassLoaderMap.containsKey(namespace)
                && Objects.nonNull(sandboxClassLoaderMap.get(namespace))) {
            classLoader = sandboxClassLoaderMap.get(namespace);
        }

        // 如果未启动则重新加载
        else {
            classLoader = new SandboxClassLoader(namespace, coreJar);
            sandboxClassLoaderMap.put(namespace, classLoader);
        }

        return classLoader;
    }

    private static String getUtKillerHome(final Map<String, String> featureMap) {
        String home = featureMap.getOrDefault("utkiller_home", DEFAULT_UTKILLER_HOME);;
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
        return home + File.separatorChar + "utkiller-core" + File.separator + "target" + File.separator + "utkiller-core-1.0.0-SNAPSHOT.jar";
    }
}
