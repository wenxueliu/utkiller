package com.ut.killer.spy;

import com.ut.killer.command.SpyAPI;

import java.lang.reflect.Method;

public class SpyUtils {
    public static void cleanUpSpyReference() {
        try {
            SpyAPI.setNopSpy();
            SpyAPI.destroy();
        } catch (Throwable e) {
            // ignore
        }
        // AgentBootstrap.resetArthasClassLoader();
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.ut.killer.Main");
            Method method = clazz.getDeclaredMethod("resetArthasClassLoader");
            method.invoke(null);
        } catch (Throwable e) {
            // ignore
        }
    }
}
