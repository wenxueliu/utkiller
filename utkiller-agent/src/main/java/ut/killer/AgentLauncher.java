package ut.killer;

import com.ut.killer.http.HttpAgentServer;
import ut.killer.config.UTKillerConfiguration;
import ut.killer.utils.ArgsUtils;
import ut.killer.utils.YamlUtils;

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
            start(inst, configPath);
        } catch (Throwable cause) {
            throw new RuntimeException("utkiller attach failed.", cause);
        }
    }

    private static void start(Instrumentation inst, String configPath) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            HttpAgentServer.begin(configPath, inst);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}