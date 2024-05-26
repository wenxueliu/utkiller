package com.ut.killer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class JavaParser {
    private static final Logger logger = LoggerFactory.getLogger(JavaParser.class);

    public static Map<String, Set<String>> class2package = new HashMap<>();

    static  {
        HashSet<String> values = new HashSet<>();
        values.add("org.apache.catalina.connector");
        class2package.put("javax.servlet.http.HttpServletRequest", values);
    }

//    public static void dump(String className) throws ClassNotFoundException {
//        logger.info("before dump: {}", className);
//        String packageName = className.substring(0, className.lastIndexOf("."));
//        logger.info("packageName:{}  className:{}", packageName, className);
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(ClasspathHelper.forPackage(packageName))
//                .setScanners(new SubTypesScanner(false))
//                .filterInputsBy(new FilterBuilder().includePackage(packageName)));
//        Class<?> classType1 = Class.forName(className);
//        for (Class<?> classType : reflections.getSubTypesOf(classType1)) {
//            logger.info("classType: {}", classType);
//        }
//    }

//    public static Reflections getReflections(String className) {
//        if (className.lastIndexOf(".") < 1) {
//            throw new IllegalArgumentException("please give canonical class name");
//        }
//        String packageName = className.substring(0, className.lastIndexOf("."));
//        logger.info("packageName:{}  className:{}", packageName, className);
//        Set<String> packageNames = class2package.getOrDefault(className, Sets.newHashSet(packageName));
//        Collection<URL> urls = packageNames.stream().map(ClasspathHelper::forPackage).flatMap(Collection::stream).collect(Collectors.toList());
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setUrls(urls)
//                .setScanners(new SubTypesScanner(false))
//                .filterInputsBy(new FilterBuilder().includePackage(packageNames.toArray(new String[0]))));
//        return reflections;
//    }
}
