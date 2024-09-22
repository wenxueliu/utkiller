package ut.killer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理类加载器的工具类。
 * <p>
 * 此类提供了用于注册、获取和删除类加载器的方法，旨在简化应用程序中类加载器管理的复杂性。
 * </p>
 */
public class ClassLoaderManager {
    private static final Map<String, ClassLoader> namespace2ClassLoader
            = new ConcurrentHashMap<>();

    /**
     * 获取指定命名空间下的类加载器，如果不存在则定义并缓存新的类加载器。
     *
     * <p>
     * 此方法是线程安全的，确保在并发环境下也能正确地获取或定义类加载器。
     * 如果指定命名空间已存在类加载器，则直接返回该类加载器；
     * 否则，将传入的类加载器关联到指定命名空间并返回。
     * </p>
     *
     * @param namespace          指定的命名空间标识符。
     * @param definedClassLoader 要定义的类加载器实例。
     * @return 返回指定命名空间对应的类加载器。
     */
    public static synchronized ClassLoader getOrDefine(String namespace, ClassLoader definedClassLoader) {
        ClassLoader classLoader = namespace2ClassLoader.get(namespace);
        if (Objects.isNull(classLoader)) {
            classLoader = definedClassLoader;
            namespace2ClassLoader.put(namespace, classLoader);
        }
        return classLoader;
    }

    /**
     * 获取指定命名空间下的类加载器。
     *
     * <p>
     * 此方法用于从缓存中检索与指定命名空间关联的类加载器。
     * 如果没有找到对应的类加载器，则返回 {@code null}。
     * </p>
     *
     * @param namespace 要获取类加载器的命名空间标识符。
     * @return 与指定命名空间相关的类加载器，如果没有找到则返回 {@code null}。
     */
    public static ClassLoader getClassLoader(String namespace) {
        return namespace2ClassLoader.get(namespace);
    }
}