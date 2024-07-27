package com.ut.killer.parser;

import com.ut.killer.bytekit.ByteTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Scan {
    private static final Logger logger = LoggerFactory.getLogger(ByteTransformer.class);

    public static final String configFile = resolveProperty("utkiller.config.file", (String) null);


    static <T> T resolveProperty(String propName,
                                 Function<String, T> resolvingFunc,
                                 T defaultValue) {
        T value = defaultValue;
        try {
            String envVar = propName.toUpperCase().replace('.', '_');
            String envVal = System.getenv(envVar);
            String propVal = System.getProperty(propName);
            String propValue = propVal != null ? propVal : envVal;
            if (propValue != null) {
                value = resolvingFunc.apply(propValue);
            }
        } catch (Exception ex) {
            logger.error("failed to resolve {}, falling back to default\n", propName, ex);
            value = defaultValue;
        }
        return value;
    }

    static String[] resolveProperty(String propName, String[] defaultValue) {
        String[] value = defaultValue;
        try {
            final String propValue = System.getProperty(propName);
            if (propValue != null) {
                value = propValue.split(",");
                assert (value.length < 2);
            }
        } catch (Exception ex) {
            // Logger may not be configured yet, so use System.err here to
            // report problems.
            logger.error("failed to resolve {}, falling back to default\n", propName, ex);
        }
        return value;
    }

    static Boolean resolveProperty(String propName, Boolean defaultValue) {
        return resolveProperty(propName, Boolean::valueOf, defaultValue);
    }

    static Integer resolveProperty(String propName, Integer defaultValue) {
        return resolveProperty(propName, Integer::valueOf, defaultValue);
    }

    static String resolveProperty(String propName, String defaultValue) {
        return resolveProperty(propName, Object::toString, defaultValue);
    }

    public static void main(String[] args) throws IOException {
        String directory = "/Users/liuwenxue/Documents/mycomputer/mygithub/zpai_backend/";
        Path javaDir = Paths.get(directory).resolve("src/main/java");
        System.out.println(javaDir);
        System.out.println(javaDir.getNameCount());
        if (Files.isDirectory(javaDir)) {
            int pkgStart = javaDir.getNameCount();
            // Collect package names in src/main/java
            Set<Path> packages = new HashSet<>();
            Files.walkFileTree(javaDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".java")) {
                        int pkgEnd = file.getParent().getNameCount();
                        logger.debug("pkgEnd {}", pkgEnd);

                        if (pkgStart == pkgEnd) {
                            logger.debug("xx.java parent() {}", file.getParent());
                            // We're in the the unnamed package, ignore
                            return FileVisitResult.CONTINUE;
                        }

                        Path packagePath = file.getParent().subpath(pkgStart, pkgEnd);
                        if (packagePath.getNameCount() > 0) {
                            logger.debug("path {}", packagePath);
                            packages.add(packagePath);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            logger.info("packages {}", packages);
            // Collect just the packages that don't have a matching ancestor in the package
            // list.
            List<Path> topLevelPackagePaths = packages.stream().sorted().collect(ArrayList::new, (memo, packagePath) -> {
                logger.info("{} packagePath {} {}", memo, packagePath, packagePath.getNameCount());
                for (int i = 1; i < packagePath.getNameCount(); i++) {
                    Path ancestorPath = packagePath.subpath(0, i);
                    if (memo.contains(ancestorPath)) {
                        return;
                    }
                }
                memo.add(packagePath);
            }, ArrayList::addAll);
            logger.info("topLevelPackages {}", topLevelPackagePaths);
            topLevelPackagePaths.stream().map(packagePath -> {
                List<String> tokens = new ArrayList<>();
                for (int i = 0; i < packagePath.getNameCount(); i++) {
                    tokens.add(packagePath.getName(i).toString());
                }
                String path = String.join(".", tokens);
                return path;
            }).collect(Collectors.toList());

        } else {
            logger.info("{} folder was not found in your project, so no packages were auto-detected.", javaDir);
        }
    }
}
