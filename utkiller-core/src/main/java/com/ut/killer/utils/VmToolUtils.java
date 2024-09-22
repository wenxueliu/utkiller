package com.ut.killer.utils;

public class VmToolUtils {
    private static String libName = null;
    static {
        if (OSUtils.isLinux()) {
            libName = "libArthasJniLibrary-x64.so";
        }
        if (OSUtils.isWindows()) {
            libName = "libArthasJniLibrary-x64.dll";
        }
    }

    public static String detectLibName() {
        return libName;
    }
}