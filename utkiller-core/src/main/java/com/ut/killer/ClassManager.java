package com.ut.killer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ClassManager 是一个用于管理类与其方法名集合的工具类。
 * <p>
 * 它提供了存储、检索、更新和删除类与方法名映射的功能。通过维护一个内部的哈希映射，
 * 该类允许用户记录特定类的所有方法名，并可以对这些信息进行操作。
 * </p>
 */
public class ClassManager {
    private static Map<String, Set<String>> class2Methods = new HashMap<>();

    /**
     * 将给定的类名及其方法名集合添加到映射中。
     *
     * @param className   要添加的类的全限定名。
     * @param methodNames 要添加的方法名集合。
     */
    public static void putClass(String className, Set<String> methodNames) {
        class2Methods.put(className, methodNames);
    }

    /**
     * 检查指定的类名是否存在于映射中。
     *
     * @param className 要检查的类的全限定名。
     * @return 如果类名存在于映射中，则返回 true；否则返回 false。
     */
    public static boolean containsClass(String className) {
        return class2Methods.containsKey(className);
    }

    /**
     * 从映射中移除指定的类名及其相关方法名集合。
     *
     * @param className 要移除的类的全限定名。
     */
    public static void removeClass(String className) {
        class2Methods.remove(className);
    }

    /**
     * 获取指定类名对应的方法名集合。
     *
     * @param className 要获取方法名集合的类的全限定名。
     * @return 如果类名存在于映射中，则返回其对应的方法名集合；否则返回 null。
     */
    public static Set<String> getMethods(String className) {
        return class2Methods.get(className);
    }

    /**
     * 向指定类名的方法名集合中添加一个新的方法名。
     *
     * @param className  要添加方法名的类的全限定名。
     * @param methodName 要添加的新方法名。
     */
    public static void addMethod(String className, String methodName) {
        class2Methods.get(className).add(methodName);
    }

    /**
     * 从指定类名的方法名集合中移除一个方法名。
     *
     * @param className  要移除方法名的类的全限定名。
     * @param methodName 要移除的方法名。
     */
    public static void removeMethod(String className, String methodName) {
        class2Methods.get(className).remove(methodName);
    }

    /**
     * 清空映射中的所有条目。
     */
    public static void clear() {
        class2Methods.clear();
    }

    /**
     * 获取映射中的所有条目。
     *
     * @return 映射中的所有类名及其对应的方法名集合。
     */
    public static Map<String, Set<String>> getAll() {
        return class2Methods;
    }
}