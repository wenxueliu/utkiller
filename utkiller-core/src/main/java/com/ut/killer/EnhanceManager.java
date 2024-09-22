package com.ut.killer;

import java.util.HashMap;
import java.util.Map;

/**
 * EnhanceManager 是用于管理增强类字节码的工具类。
 * <p>
 * 它提供了存储、检索和移除增强类字节码的方法。此类使用内部的哈希映射来跟踪类名与它们对应的字节码。
 * 主要用途可能包括但不限于：
 * - 在运行时动态修改类的字节码；
 * - 存储和恢复由某些机制增强过的类定义。
 * </p>
 */
public class EnhanceManager {
    private final static Map<String, byte[]> className2Bytes = new HashMap<>();

    /**
     * 将指定的类字节码与类名关联并存入内部映射。
     *
     * @param className        要存储的类的全限定名（例如 "com.example.MyClass"）。
     * @param originClassBytes 要存储的类字节码。
     */
    public static synchronized void put(String className, byte[] originClassBytes) {
        className2Bytes.put(className, originClassBytes);
    }

    /**
     * 获取指定类名对应的字节码。
     *
     * @param className 要获取字节码的类的全限定名。
     * @return 如果存在，则返回指定类名的字节码；否则返回 null。
     */
    public static synchronized byte[] get(String className) {
        return className2Bytes.get(className);
    }

    /**
     * 判断指定类名是否存在于内部映射中。
     *
     * @param className 要检查的类的全限定名。
     * @return 如果内部映射包含指定类名，则返回 true；否则返回 false。
     */
    public static boolean contains(String className) {
        return className2Bytes.containsKey(className);
    }

    /**
     * 移除指定类名对应的字节码。
     *
     * @param className 要移除的类的全限定名。
     * @return 如果成功移除，则返回被移除的字节码；否则返回 null。
     */
    public static synchronized byte[] remove(String className) {
        return className2Bytes.remove(className);
    }
}