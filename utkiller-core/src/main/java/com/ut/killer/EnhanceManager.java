package com.ut.killer;

import java.util.Map;

public class EnhanceManager {
    private static Map<String, byte[]> enhanceClasses;

    public static synchronized void put(String className, byte[] originClassBytes) {
        enhanceClasses.put(className, originClassBytes);
    }

    public static synchronized byte[] get(String className) {
        return enhanceClasses.get(className);
    }

    public static synchronized byte[] remove(String className) {
        return enhanceClasses.remove(className);
    }
}