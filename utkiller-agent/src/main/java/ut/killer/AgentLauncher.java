package ut.killer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

/**
 * 启动 Java Agent 的工具类。
 * <p>
 * 此类提供了启动 Java Agent 的方法，通常用于在 JVM 启动时附加额外的功能。
 * </p>
 */
public class AgentLauncher {
    /**
     * 在Java应用启动前被调用的方法，此方法允许对尚未启动的Java应用中的类进行转换或其它操作。
     *
     * @param args 在命令行中传递给代理的参数（如果有）。这些参数可以通过 -javaagent:<agent>=<args> 的形式传递。
     * @param inst 一个Instrumentation实例，允许程序动态地加载类、重新定义类以及卸载类等。
     */
    public static void premain(String args, Instrumentation inst) {
        start(args, inst);
    }

    /**
     * 在Java应用运行时被JVM调用的方法，此方法允许对正在运行的Java应用中的类进行转换或其他操作。
     *
     * <p>此方法主要用于那些在应用程序启动后才被加载的代理（agent）。</p>
     *
     * @param args 在命令行中传递给代理的参数（如果有）。这些参数可以通过 -javaagent:<agent>=<args> 的形式传递。
     * @param inst 一个Instrumentation实例，允许程序动态地加载类、重新定义类以及卸载类等。
     */
    public static void agentmain(String args, Instrumentation inst) {
        start(args, inst);
    }

    private static void start(String args, Instrumentation inst) {
        String configPath = ArgsUtils.toMap(args).getOrDefault("configPath", "");
        UTKillerConfiguration config = YamlUtils.parse(configPath);
        String namespace = config.getNamespace();
        try {
            ClassLoader classLoader = ClassLoaderManager.getOrDefine(namespace, Thread.currentThread().getContextClassLoader());
            start(inst, classLoader, config);
        } catch (Throwable cause) {
            throw new RuntimeException("utkiller attach failed.", cause);
        }
    }

    private static void start(Instrumentation inst, ClassLoader classLoader, UTKillerConfiguration config) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        classLoader.loadClass("fi.iki.elonen.NanoHTTPD");
        Class<?> httpAgentServer = classLoader.loadClass("com.ut.killer.http.HttpAgentServer");
        httpAgentServer.getMethod("begin", UTKillerConfiguration.class, Instrumentation.class).invoke(null, config, inst);
    }
}