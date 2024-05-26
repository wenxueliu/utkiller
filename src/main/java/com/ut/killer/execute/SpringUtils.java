package com.ut.killer.execute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpringUtils {
    private static final Logger logger = LoggerFactory.getLogger(SpringUtils.class);

    private static Object applicationContext;
    private static Object springTestContextManager;
    private static boolean isSpringPresent;
    private static Method getBeanMethod;
    private static Method getBeanByBeanNameMethod;
    private static Object springBeanFactory;
    private static Method getBeanDefinitionNamesMethod;

    public static Object getBean(String targetClassName) {
        loadContext(Thread.currentThread().getContextClassLoader());
        Object objectInstanceByClass = null;
        if (applicationContext != null && getBeanMethod != null) {
            try {
                objectInstanceByClass = getBeanMethod.invoke(applicationContext,
                        Class.forName(targetClassName));
            } catch (Exception ex) {
                logger.error("instance by getBean error", ex);
            }
        }
        return objectInstanceByClass;
    }

    private static void loadContext(ClassLoader classLoader) {
        try {
            if (applicationContext != null) {
                // already loaded
                return;
            }
            if (springTestContextManager == null) {
                springTestContextManager = InstanceUtils.springContextInstance("org.springframework.boot.web" +
                        ".reactive.context" +
                        ".AnnotationConfigReactiveWebServerApplicationContext");
                setSpringApplicationContextAndLoadBeanFactory(springTestContextManager);
            }

            if (springTestContextManager == null) {
                springTestContextManager = InstanceUtils.springContextInstance("org.springframework.boot.web" +
                        ".servlet.context.AnnotationConfigServletWebServerApplicationContext");
                setSpringApplicationContextAndLoadBeanFactory(springTestContextManager);
            }

            if (applicationContext != null) {
                Class<?> applicationContextClass = Class.forName("org.springframework.context.ApplicationContext");
                getBeanDefinitionNamesMethod = applicationContextClass.getMethod("getBeanNamesForType", Class.class,
                        boolean.class, boolean.class);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Object setSpringApplicationContextAndLoadBeanFactory(Object applicationContextArg) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            if (applicationContextArg == null) {
                return null;
            }
            applicationContext = applicationContextArg;

            Class<?> applicationContextClass = Class.forName("org.springframework.context.ApplicationContext");
            getBeanMethod = applicationContextClass.getMethod("getBean", Class.class);
            getBeanByBeanNameMethod = applicationContextClass.getMethod("getBean", String.class);
            Method getAutowireCapableBeanFactoryMethod = applicationContextClass.getMethod(
                    "getAutowireCapableBeanFactory");

            springBeanFactory = Class.forName("org.springframework.beans.factory.support.DefaultListableBeanFactory")
                    .cast(getAutowireCapableBeanFactoryMethod.invoke(applicationContext));
            return springBeanFactory;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public void enableSpringIntegration(Class<?> testClass) {
        if (springTestContextManager == null) {
            trySpringIntegration(testClass);
        }
    }

    private void trySpringIntegration(Class<?> testClass) {
        // spring loader
        // if spring exists
        try {
            Class.forName("org.springframework.boot.SpringApplication");
            isSpringPresent = true;


            Annotation[] classAnnotations = testClass.getAnnotations();
            boolean hasEnableAutoConfigAnnotation = false;
            for (Annotation classAnnotation : classAnnotations) {
                if (classAnnotation.annotationType().getCanonicalName().startsWith("org.springframework.")) {
                    hasEnableAutoConfigAnnotation = true;
                    break;
                }
            }
            Class<?> testContextManagerClass = null;
            try {
                testContextManagerClass = Class.forName("org.springframework.test.context.TestContextManager");
            } catch (Exception e) {
            }
            // no spring context creation if no spring annotation is used on the test class
            if (!hasEnableAutoConfigAnnotation) {
                return;
            }

            springTestContextManager = testContextManagerClass.getConstructor(Class.class).newInstance(testClass);
            Method getTestContextMethod = testContextManagerClass.getMethod("getTestContext");
            Class<?> testContextClass = Class.forName("org.springframework.test.context.TestContext");

            Method getApplicationContextMethod = testContextClass.getMethod("getApplicationContext");


            Class<?> pspcClass = Class.forName(
                    "org.springframework.context.support.PropertySourcesPlaceholderConfigurer");

            Object propertySourcesPlaceholderConfigurer = pspcClass.getConstructor().newInstance();


            Class<?> propertiesClass = Class.forName("java.util.Properties");
            Method pspcClassSetPropertiesMethod = pspcClass.getMethod("setProperties", propertiesClass);

//            PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
            Class<?> yamlPropertiesFactoryBeanClass = Class.forName(
                    "org.springframework.beans.factory.config.YamlPropertiesFactoryBean");
            Object yaml = yamlPropertiesFactoryBeanClass.getConstructor().newInstance();
            Method yamlGetObjectMethod = yamlPropertiesFactoryBeanClass.getMethod("getObject");
            Class<?> classPathResourceClass = Class.forName("org.springframework.core.io.ClassPathResource");
            Object classPathResource = classPathResourceClass.getConstructor(String.class)
                    .newInstance("config/application.yml");
//            ClassPathResource classPathResource = new ClassPathResource("config/application.yml");
            Method setResourceMethod = yamlPropertiesFactoryBeanClass.getMethod("setResources",
                    Class.forName("[Lorg.springframework.core.io.Resource;"));
            Method resourceExistsMethod = classPathResourceClass.getMethod("exists");
            if ((boolean) resourceExistsMethod.invoke(classPathResource)) {
//                yaml.setResources(classPathResource);
                setResourceMethod.invoke(yaml, classPathResource);
//                propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
                Object yamlObject = yamlGetObjectMethod.invoke(yaml);
                pspcClassSetPropertiesMethod.invoke(propertySourcesPlaceholderConfigurer, yamlObject);
            }


            Object testContext = getTestContextMethod.invoke(this.springTestContextManager);
            Object applicationContext = getApplicationContextMethod.invoke(testContext);

            Object factory = setSpringApplicationContextAndLoadBeanFactory(applicationContext);

            Method pspcProcessBeanFactoryMethod = pspcClass.getMethod("postProcessBeanFactory",
                    Class.forName("org.springframework.beans.factory.config.ConfigurableListableBeanFactory"));

            pspcProcessBeanFactoryMethod.invoke(propertySourcesPlaceholderConfigurer, factory);

//            propertySourcesPlaceholderConfigurer.postProcessBeanFactory(
//                    (DefaultListableBeanFactory) this.springTestContextManager.getTestContext().getApplicationContext()
//                            .getAutowireCapableBeanFactory());
        } catch (Throwable e) {
            // failed to start spring application context
            e.printStackTrace();
            return;
        }
    }
}
