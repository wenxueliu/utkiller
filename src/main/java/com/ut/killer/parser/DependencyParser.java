package com.ut.killer.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.ast.CompilationUnit;
import com.ut.killer.parser.sourcecode.ClassInfo;
import com.ut.killer.parser.sourcecode.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DependencyParser {
    private static final Logger logger = LoggerFactory.getLogger(DependencyParser.class);

    public static void parse(String sourceDirectory) {
        // 指定要解析的源代码目录
        try {
            // 存储所有类的信息
            List<ClassInfo> classInfos = new ArrayList<>();
            // 递归解析源代码目录中的所有Java文件
            Files.walk(Paths.get(sourceDirectory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            FileInputStream in = new FileInputStream(path.toFile());
                            CompilationUnit cu = null; //JavaParser().parse(in);
                            ClassInfo classInfo = new ClassInfo();
                            cu.accept(new ClassVisitor(), classInfo);
                            classInfos.add(classInfo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            // 序列化类信息为JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonResult = objectMapper.writeValueAsString(classInfos);
            System.out.println(jsonResult);
        } catch (IOException ex) {
            logger.error("parse error ", ex);
        }
    }
}