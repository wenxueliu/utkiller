package com.ut.killer.execute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import javassist.ClassPool;
import javassist.CtClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class MethodExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MethodExecutor.class);

    private final ByteBuddy byteBuddyInstance = new ByteBuddy()
            .with(new NamingStrategy.SuffixingRandom("utkiller"));

    ObjectMapper objectMapper = new ObjectMapper();

    Objenesis objenesis = new ObjenesisStd();

    ParameterFactory parameterFactory = new ParameterFactory(objenesis, objectMapper, byteBuddyInstance);

    public void execute1(String className, String methodName, Object... args) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(className);
            Class proxyClazz = ctClass.toClass();
            Object proxyInstance = proxyClazz.newInstance();
            proxyClazz.getMethod(methodName).invoke(className, args);
        } catch (Exception ex) {
            logger.error("execute error", ex);
        }
    }

    public Object execute(String targetClassName, String methodName, String methodSignature,
                          List<String> methodParameters, List<String> parameterTypes)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ClassLoader targetClassLoader1 = Thread.currentThread().getContextClassLoader();

        Object objectInstanceByClass = tryObjectConstruct(targetClassName, targetClassLoader1, new HashMap<>());

        Class<?> targetClassType = getTargetClassType(targetClassName, targetClassLoader1, objectInstanceByClass);

        ClassLoader targetClassLoader = objectInstanceByClass.getClass().getClassLoader();

        List<String> methodSignatureParts = MethodSignatureParser.parseMethodSignature(
                methodSignature);
        String methodReturnType = methodSignatureParts.remove(methodSignatureParts.size() - 1);

        JavaType[] expectedMethodArgumentTypes = getExpectedMethodArgumentTypes(methodSignatureParts);

        // gets a method or throws exception if no such method
        Method methodToExecute = getMethodToExecute(targetClassType, methodName,
                expectedMethodArgumentTypes);
        // we know more complex ways to do bypassing the security checks this thanks to lombok
        // but for now this will do
        methodToExecute.setAccessible(true);

        Class<?>[] parameterTypesClass = methodToExecute.getParameterTypes();

        Object[] parameters = buildParametersUsingTargetClass(targetClassLoader, methodParameters,
                parameterTypesClass, parameterTypes);

        Object methodReturnValue = methodToExecute.invoke(objectInstanceByClass, parameters);
        Object serializedValue = serializeMethodReturnValue(methodReturnValue);
        return serializedValue;
    }

    private static Class<?> getTargetClassType(String targetClassName, ClassLoader targetClassLoader1, Object objectInstanceByClass) {
        Class<?> targetClassType = null;
        if (objectInstanceByClass != null) {
            targetClassType = objectInstanceByClass.getClass();
        } else {
            try {
                targetClassType = Class.forName(targetClassName, false, targetClassLoader1);
            } catch (Exception ex) {
                logger.error("targetClassName {} ", targetClassName, ex);
            }
        }
        return targetClassType;
    }

    private JavaType[] getExpectedMethodArgumentTypes(List<String> methodSignatureParts) {
        JavaType[] expectedMethodArgumentTypes = new JavaType[methodSignatureParts.size()];

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        for (int i = 0; i < methodSignatureParts.size(); i++) {
            String methodSignaturePart = methodSignatureParts.get(i);
            logger.info("Method parameter [" + i + "] type: " + methodSignaturePart);
            JavaType typeReference = ClassTypeUtil
                    .getClassNameFromDescriptor(methodSignaturePart, typeFactory);
            expectedMethodArgumentTypes[i] = typeReference;
        }
        return expectedMethodArgumentTypes;
    }

    public Object serializeMethodReturnValue(Object methodReturnValue) {
        if (methodReturnValue == null) {
            return null;
        }
        if (methodReturnValue instanceof Double) {
            return Double.doubleToLongBits((Double) methodReturnValue);
        } else if (methodReturnValue instanceof Float) {
            return Float.floatToIntBits((Float) methodReturnValue);
        } else if (methodReturnValue instanceof String) {
            return methodReturnValue;
        } else {
            try {
                if (methodReturnValue instanceof Flux) {
                    Flux<?> returnedFlux = (Flux<?>) methodReturnValue;

                    CountDownLatch cdl = new CountDownLatch(1);
                    StringBuffer returnValue = new StringBuffer();

                    returnedFlux
                            .collectList()
                            .doOnError(e -> {
                                try {
                                    e.printStackTrace();
                                    returnValue.append(objectMapper.writeValueAsString(e));
                                } catch (JsonProcessingException ex) {
                                    returnValue.append(e.getMessage());
                                } finally {
                                    cdl.countDown();
                                }
                            })
                            .subscribe(e -> {
                                try {
                                    returnValue.append(objectMapper.writeValueAsString(e));
                                } catch (JsonProcessingException ex) {
                                    try {
                                        returnValue.append(objectMapper.writeValueAsString(ex));
                                    } catch (JsonProcessingException exc) {
                                        returnValue.append(ex.getMessage());
                                    }
                                } finally {
                                    cdl.countDown();
                                }
                            });
                    cdl.await();
                    return returnValue.toString();

                } else if (methodReturnValue instanceof Mono) {
                    Mono<?> returnedMono = (Mono<?>) methodReturnValue;
                    CountDownLatch cdl = new CountDownLatch(1);
                    StringBuffer returnValue = new StringBuffer();

                    returnedMono
                            .log()
                            .subscribe(e -> {
                                try {
                                    returnValue.append(objectMapper.writeValueAsString(e));
                                } catch (JsonProcessingException ex) {
                                    try {
                                        returnValue.append(objectMapper.writeValueAsString(ex));
                                    } catch (JsonProcessingException exc) {
                                        returnValue.append(ex.getMessage());
                                    }
                                } finally {
                                    cdl.countDown();
                                }
                            }, e -> {
                                try {
                                    returnValue.append(objectMapper.writeValueAsString(e));
                                } catch (JsonProcessingException ex) {
                                    try {
                                        returnValue.append(objectMapper.writeValueAsString(ex));
                                    } catch (JsonProcessingException exc) {
                                        returnValue.append(ex.getMessage());
                                    }
                                } finally {
                                    cdl.countDown();
                                }
                            }, cdl::countDown);
                    cdl.await();
                    return returnValue.toString();

                } else if (methodReturnValue instanceof Future) {
                    methodReturnValue = ((Future<?>) methodReturnValue).get();
                }
                return objectMapper.writeValueAsString(methodReturnValue);
            } catch (Exception ide) {
                return "{\"className\": \"" + methodReturnValue.getClass().getCanonicalName() + "\"}";
            }
        }
    }

    private Object[] buildParametersUsingTargetClass(
            ClassLoader targetClassLoader,
            List<String> methodParameters,
            Class<?>[] parameterTypesClass,
            List<String> parameterTypes) {
        TypeFactory typeFactory = new ObjectMapper().getTypeFactory().withClassLoader(targetClassLoader);
        Object[] parameters = new Object[methodParameters.size()];

        for (int i = 0; i < methodParameters.size(); i++) {
            String methodParameterStringValue = methodParameters.get(i);
            Class<?> parameterType = parameterTypesClass[i];
            String parameterTypeName = parameterTypes.get(i);
            Object parameterObject;
            try {
                parameterObject = parameterFactory.createObjectInstanceFromStringAndTypeInformation(parameterTypeName,
                        methodParameterStringValue, parameterType, typeFactory);
            } catch (Exception e) {
                System.err.println(
                        "Failed to create paramter of type [" + parameterTypeName + "] from source " + methodParameterStringValue + " => " + e.getMessage());
                e.printStackTrace();
                parameterObject = null;
            }

            parameters[i] = parameterObject;
        }
        return parameters;
    }


    private Method getMethodToExecute(Class<?> objectClass, String expectedMethodName,
                                      JavaType[] expectedMethodArgumentTypes)
            throws NoSuchMethodException {

        StringBuilder className = new StringBuilder();

        Method methodToExecute = null;
        List<String> methodNamesList = new ArrayList<>();
        while (objectClass != null && !objectClass.equals(Object.class)) {

            className.append(objectClass.getCanonicalName()).append(", ");
            int argsCount = expectedMethodArgumentTypes.length;
            try {
                Class<?>[] paramClassNames = new Class[argsCount];
                for (int i = 0; i < argsCount; i++) {
                    Class<?> rawClass = expectedMethodArgumentTypes[i].getRawClass();
                    paramClassNames[i] = rawClass;
                }
                methodToExecute = objectClass.getDeclaredMethod(expectedMethodName, paramClassNames);
            } catch (NoSuchMethodException ignored) {

            }

            if (methodToExecute == null) {
                Method[] methods = objectClass.getDeclaredMethods();
                for (Method method : methods) {
                    String methodName = method.getName();
                    methodNamesList.add(methodName);
                    if (methodName.equals(expectedMethodName)
                            && method.getParameterCount() == argsCount) {

                        Class<?>[] actualParameterTypes = method.getParameterTypes();

                        boolean match = true;
                        for (int i = 0; i < argsCount; i++) {
                            Class<?> methodParameterType = expectedMethodArgumentTypes[i].getRawClass();
                            Class<?> actualParamType = actualParameterTypes[i];
                            if (!actualParamType.getCanonicalName()
                                    .equals(methodParameterType.getCanonicalName())) {
                                match = false;
                                break;
                            }
                        }

                        if (match) {
                            methodToExecute = method;
                            break;
                        }

                    }
                }
            }
            if (methodToExecute != null) {
                break;
            }
            objectClass = objectClass.getSuperclass();
        }
        if (methodToExecute == null) {
            System.err.println("Method not found: " + expectedMethodName
                    + ", methods were: " + methodNamesList);
            throw new NoSuchMethodException("method not found [" + expectedMethodName
                    + "] in class [" + className + "]. Available methods are: "
                    + methodNamesList);
        }

        return methodToExecute;
    }

    private Object tryObjectConstruct(String className, ClassLoader targetClassLoader, Map<String, Object> buildMap)
            throws IllegalAccessException {
        if (className.equals("java.util.List")) {
            return new ArrayList<>();
        }
        if (className.equals("java.util.Map")) {
            return new HashMap<>();
        }
        if (targetClassLoader == null) {
            System.err.println("Failed to construct instance of class [" + className + "]. classLoader is not defined");
        }
        Object newInstance = SpringUtils.getBean(className);
        if (newInstance != null) {
            return newInstance;
        }
        Class<?> loadedClass;
        try {
            loadedClass = targetClassLoader.loadClass(className);
        } catch (ClassNotFoundException classNotFoundException) {
            // class not found
            // or this is an internal class ? try to check one level up class ?
            if (className.lastIndexOf(".") == -1) {
                // todo: if it was an array of an internal class
                // com.something.package.ParentClass$ChildClass[][]
                logger.error("class not find error", classNotFoundException);
                return null;
            }
            String parentName = className.substring(0, className.lastIndexOf("."));
            try {
                Class<?> parentClassType = targetClassLoader.loadClass(parentName);
                // if we found this, then
                loadedClass =
                        targetClassLoader.loadClass(
                                parentName + "$" + className.substring(className.lastIndexOf(".") + 1));

            } catch (ClassNotFoundException cne) {
                // try another level ? just to be sure ?better way to identify internal classes ?
                return null;
            }
        }
        Constructor<?> noArgsConstructor = null;
        noArgsConstructor = null;
        Constructor<?>[] declaredConstructors = loadedClass.getDeclaredConstructors();
        if (declaredConstructors.length > 0) {
            noArgsConstructor = declaredConstructors[0];
        }
        for (Constructor<?> declaredConstructor : declaredConstructors) {
            if (declaredConstructor.getParameterCount() == 0) {
                noArgsConstructor = declaredConstructor;
                break;
            }
        }
        try {
            noArgsConstructor.setAccessible(true);

            int paramCount = noArgsConstructor.getParameterCount();
            Class<?>[] paramTypes = noArgsConstructor.getParameterTypes();
            Object[] parameters = new Object[paramCount];
            for (int i = 0; i < paramCount; i++) {
                String typeName = paramTypes[i].getCanonicalName();
                Object paramValue = buildMap.get(typeName);
                if (paramValue == null) {
                    paramValue = tryObjectConstruct(typeName, targetClassLoader, buildMap);
                }
                parameters[i] = paramValue;
            }

            newInstance = noArgsConstructor.newInstance(parameters);
        } catch (Throwable ex) {
            logger.error("error:", ex);
        }


        if (newInstance == null) {
            Method[] methods = loadedClass.getMethods();
            // try to get the instance of the class using Singleton.getInstance
            for (Method method : methods) {
                if (method.getParameterCount() == 0 && Modifier.isStatic(method.getModifiers())) {
                    if (method.getReturnType().equals(loadedClass)) {
                        try {
                            return method.invoke(null);
                        } catch (InvocationTargetException ex) {
                            logger.error("method.invoke error", ex);
                            // this method for potentially getting instance from static getInstance type method
                            // did not work
                        }
                    }
                }
            }
        }
        if (newInstance == null) {
            try {
                newInstance = objenesis.newInstance(loadedClass);
            } catch (java.lang.InstantiationError | IllegalAccessError e) {
                // failed to create using objenesis
            }
        }

        buildMap.put(className, newInstance);
        if (newInstance == null) {
            return newInstance;
        }

        // field injections
        Class<?> fieldsForClass = loadedClass;

        while (fieldsForClass != null && !fieldsForClass.equals(Object.class)) {
            Field[] fields = fieldsForClass.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                } catch (Exception e) {
                    continue;
                }

                String fieldTypeName = field.getType().getCanonicalName();
                Object value = field.get(newInstance);
                if (value != null) {
                    continue;
                }
                if (buildMap.containsKey(fieldTypeName)) {
                    value = buildMap.get(fieldTypeName);
                } else {
                    value = tryObjectConstruct(fieldTypeName, targetClassLoader, buildMap);
                    if (value == null) {
                        continue;
                    }
                    buildMap.put(fieldTypeName, value);
                }
                try {
                    field.set(newInstance, value);
                } catch (Throwable th) {
                    logger.error("filed.set error", th);
                    System.out.println("Failed to set field value: " + th.getMessage());
                }
            }
            fieldsForClass = fieldsForClass.getSuperclass();
        }
        return newInstance;
    }
}
