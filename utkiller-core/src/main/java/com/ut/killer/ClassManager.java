package com.ut.killer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassManager {
    private static Map<String, Set<String>> class2Methods = new HashMap<>();

    public static void putClass(String className, Set<String> methodNames) {
        class2Methods.put(className, methodNames);
    }

    public static boolean containsClass(String className) {
        return class2Methods.containsKey(className);
    }

    public static void removeClass(String className) {
        class2Methods.remove(className);
    }

    public static Set<String> getMethods(String className) {
        return class2Methods.get(className);
    }

    public static void addMethod(String className, String methodName) {
        class2Methods.get(className).add(methodName);
    }

    public static void removeMethod(String className, String methodName) {
        class2Methods.get(className).remove(methodName);
    }

    public static void clear() {
        class2Methods.clear();
    }

    public static Map<String, Set<String>> getAll() {
        return class2Methods;
    }
}