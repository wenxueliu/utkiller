package com.ut.killer.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassVisitor extends VoidVisitorAdapter<ClassInfo> {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, ClassInfo classInfo) {
        classInfo.setClassName(n.getNameAsString());
        n.getFields().forEach(f -> f.accept(this, classInfo));
        n.getMethods().forEach(m -> m.accept(this, classInfo));
        // 记录继承的父类和实现的接口
        for (ClassOrInterfaceType extendedType : n.getExtendedTypes()) {
            classInfo.getDependencies().add(extendedType.getNameAsString());
        }

        for (ClassOrInterfaceType implementedType : n.getImplementedTypes()) {
            classInfo.getDependencies().add(implementedType.getNameAsString());
        }

        // 处理内部类和嵌套类
        n.getMembers().forEach(member -> {
            if (member instanceof ClassOrInterfaceDeclaration || member instanceof EnumDeclaration) {
                member.accept(this, classInfo);
            }
        });

        // 处理注解
        for (AnnotationExpr annotation : n.getAnnotations()) {
            classInfo.getDependencies().add(annotation.getNameAsString());
        }
        super.visit(n, classInfo);
    }

    @Override
    public void visit(EnumDeclaration n, ClassInfo classInfo) {
        classInfo.setClassName(n.getNameAsString());
        n.getEntries().forEach(entry -> {
            entry.getAnnotations().forEach(annotation -> classInfo.getDependencies().add(annotation.getNameAsString()));
        });
        // 处理注解
        for (AnnotationExpr annotation : n.getAnnotations()) {
            classInfo.getDependencies().add(annotation.getNameAsString());
        }
        super.visit(n, classInfo);
    }

    @Override

    public void visit(FieldDeclaration n, ClassInfo classInfo) {
        n.getVariables().forEach(var -> {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setName(var.getNameAsString());
            fieldInfo.setType(n.getElementType().asString());
            classInfo.getFields().add(fieldInfo);
            // 记录字段类型的依赖
            classInfo.getDependencies().add(n.getElementType().asString());
        });

        // 处理字段上的注解
        for (AnnotationExpr annotation : n.getAnnotations()) {
            classInfo.getDependencies().add(annotation.getNameAsString());
        }
        super.visit(n, classInfo);
    }

    @Override
    public void visit(MethodDeclaration n, ClassInfo classInfo) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(n.getNameAsString());
        methodInfo.setReturnType(n.getType().asString());
        n.getParameters().forEach(p -> methodInfo.getParameterTypes().add(p.getType().asString()));
        classInfo.getMethods().add(methodInfo);
        // 记录方法返回类型和参数类型的依赖
        classInfo.getDependencies().add(n.getType().asString());
        n.getParameters().forEach(p -> classInfo.getDependencies().add(p.getType().asString()));
        // 处理方法上的注解
        for (AnnotationExpr annotation : n.getAnnotations()) {
            classInfo.getDependencies().add(annotation.getNameAsString());
        }

        // 处理方法的throws子句
        n.getThrownExceptions().forEach(e -> classInfo.getDependencies().add(e.asString()));
        // 处理泛型方法
        n.getTypeParameters().forEach(tp -> classInfo.getDependencies().add(tp.getNameAsString()));
        // 处理方法体中的局部变量类型
        if (n.getBody().isPresent()) {
            n.getBody().get().accept(this, classInfo);
        }
        super.visit(n, classInfo);
    }

    @Override
    public void visit(VariableDeclarationExpr n, ClassInfo classInfo) {
        n.getVariables().forEach(var -> classInfo.getDependencies().add(var.getType().asString()));
        super.visit(n, classInfo);
    }

    @Override
    public void visit(LambdaExpr n, ClassInfo classInfo) {
        n.getBody().accept(this, classInfo);
        super.visit(n, classInfo);
    }

    @Override
    public void visit(ObjectCreationExpr n, ClassInfo classInfo) {
        classInfo.getDependencies().add(n.getType().asString());
        super.visit(n, classInfo);
    }

    @Override
    public void visit(BlockStmt n, ClassInfo classInfo) {
        n.getStatements().forEach(stmt -> stmt.accept(this, classInfo));
        super.visit(n, classInfo);
    }
}