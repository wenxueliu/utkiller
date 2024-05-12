package com.ut.killer.parser;

import com.ut.killer.http.hander.HttpHandler;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class ClazzUtils {
    private static final Logger logger = LoggerFactory.getLogger(ClazzUtils.class);

    public static void main(String[] args) {
        String packageName = "com.ut.killer.http";
        String interfaceName = "com.ut.killer.http.hander.HttpHandler";
//        List<Class<?>> implementingClasses = findImplementingClasses(packageName, interfaceName);
        List<Class<?>> implementingClasses = getAllClassByInterface(HttpHandler.class);
        System.out.println("Implementing classes of " + interfaceName + ":");
        for (Class<?> implementingClass : implementingClasses) {
            System.out.println(implementingClass.getName());
        }
    }

    private static URL getURL(String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            URL url = ClassLoader.getSystemClassLoader().getResource(packagePath);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void findImplementingClassesInDirectory(File directory, List<Class<?>> implementingClasses, String interfaceName) throws ClassNotFoundException {
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles(File::isFile)) {
                if (file.getName().endsWith(".class")) {
                    String className = file.getName().replace(".class", "").replaceAll("/", ".");

                    Class<?> clazz = ClassUtils.getClass(className);

                    Class<?> interfaceClass = ClassUtils.getClass(interfaceName);
                    if (clazz != null && ClassUtils.isAssignable(interfaceClass, clazz)) {
                        implementingClasses.add(clazz);
                    }
                }
            }
            for (File subDirectory : directory.listFiles(File::isDirectory)) {
                findImplementingClassesInDirectory(subDirectory, implementingClasses, interfaceName);
            }
        }
    }

    public static List<Class<?>> getAllClassByInterface(Class clazz) {
        List<Class<?>> list = new ArrayList<>();
        // 判断是否是一个接口
        if (clazz.isInterface()) {
            try {
                List<Class<?>> allClass = getAllClass(clazz.getPackage().getName());
                for (int i = 0; i < allClass.size(); i++) {
                    // isAssignableFrom:判定此 Class 对象所表示的类或接口与指定的 Class
                    // 参数所表示的类或接口是否相同，或是否是其超类或超接口
                    if (clazz.isAssignableFrom(allClass.get(i))) {
                        if (!clazz.equals(allClass.get(i))) {
                            // 自身并不加进去
                            list.add(allClass.get(i));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("出现异常{}", e.getMessage(), e);
                throw new RuntimeException("出现异常" + e.getMessage());
            }
        }
        logger.info("class list size :" + list.size());
        return list;
    }


    /**
     * 从一个指定路径下查找所有的类
     *
     * @param packageName
     */
    private static List<Class<?>> getAllClass(String packageName) {
        logger.info("packageName to search：{}", packageName);
        List<String> classNameList = getClassName(packageName);
        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNameList) {
            try {
                classes.add(Class.forName(className.replace(File.separator, ".")));
            } catch (ClassNotFoundException ex) {
                logger.error("load class from name failed: {}", className, ex);
                throw new RuntimeException("load class from name failed:" + className);
            }
        }
        logger.info("find list size :{}", classes.size());
        return classes;
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        List<String> fileNames = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            logger.debug("file type : {}", type);
            if (type.equals("file")) {
                String fileSearchPath = url.getPath();
                logger.debug("fileSearchPath: {}", fileSearchPath);
                fileSearchPath = fileSearchPath.substring(0, fileSearchPath.indexOf(File.separator + "classes"));
                logger.debug("fileSearchPath: " + fileSearchPath);
                fileNames = getClassNameByFile(fileSearchPath);
            } else if (type.equals("jar")) {
                try {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = jarURLConnection.getJarFile();
                    fileNames = getClassNameByJar(jarFile, packagePath);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("open Package URL failed：" + e.getMessage());
                }

            } else {
                throw new RuntimeException("file system not support! cannot load MsgProcessor！");
            }
        }
        return fileNames;
    }

    public static List<Class<?>> findImplementingClasses(String packageName, String interfaceName) {
        List<Class<?>> implementingClasses = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        try {
            URL url = getURL(packageName);
            if (url == null) {
                return implementingClasses;
            }
            if (url.getProtocol().equals("file")) {
                File packageDirectory = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
                findImplementingClassesInDirectory(packageDirectory, implementingClasses, interfaceName);
            } else if (url.getProtocol().equals("jar")) {
                String jarPath = URLDecoder.decode(url.getPath(), "UTF-8");
                File jarFile = new File(jarPath.substring(0, jarPath.indexOf("!")));
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory() || !entry.getName().startsWith(packagePath)
                                || !entry.getName().endsWith(".class")) {
                            continue;
                        }
                        String className = entry.getName().replaceAll(File.separator, "\\.")
                                .substring(0, entry.getName().length() - 6);
                        Class<?> clazz = ClassUtils.getClass(className);
                        Class<?> interfaceClass = ClassUtils.getClass(interfaceName);
                        if (clazz != null && ClassUtils.isAssignable(interfaceClass, clazz)) {
                            implementingClasses.add(clazz);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error while reading the JAR file", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while scanning the package", e);
        }
        return implementingClasses;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath 文件路径
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath) {
        List<String> myClassName = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                myClassName.addAll(getClassNameByFile(childFile.getPath()));
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf(File.separator + "classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace(File.separator, ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(JarFile jarFile, String packagePath) {
        List<String> myClassName = new ArrayList<>();
        try {
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                logger.info("entrys jarfile: {}", entryName);
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace(File.separator, ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                    logger.debug("Find Class {}:", entryName);
                }
            }
        } catch (Exception ex) {
            logger.error("发生异常:{}", ex);
            throw new RuntimeException("发生异常:" + ex.getMessage());
        }
        return myClassName;
    }
}
