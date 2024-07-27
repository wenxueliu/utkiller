package com.ut.killer.utils;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class AgentUtils {
    private AgentUtils() {
    }

    /**
     * 创建一个Java代理Jar文件。
     *
     * @param clazz 需要包含在Jar文件中的类，通常是一个实现了特定功能的类。
     * @return 生成的Jar文件的File对象。
     * @throws Exception 如果创建过程中遇到任何错误，将抛出异常。
     */
    public static File createJavaAgentJarFile(Class<?> clazz) throws Exception {
        File jar = File.createTempFile("agent", ".jar");
        jar.deleteOnExit();
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(new Attributes.Name("Agent-Class"), clazz.getName());
        attrs.put(new Attributes.Name("Can-Retransform-Classes"), "true");
        attrs.put(new Attributes.Name("Can-Redefine-Classes"), "true");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar.toPath()), manifest)) {
            writeClassFile(clazz, jos);
            writeClassFile(ClassPool.class, jos);
            writeClassFile(CtClass.class, jos);
        }
        return jar;
    }

    private static void writeClassFile(Class<?> clz, JarOutputStream jos) throws Exception {
        String cname = clz.getName();
        JarEntry e = new JarEntry(cname.replace('.', '/') + ".class");
        jos.putNextEntry(e);
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.get(cname);
        jos.write(clazz.toBytecode());
        jos.closeEntry();
    }
}
