package com.ut.killer.utils;

import arthas.VmTool;
import com.alibaba.bytekit.utils.IOUtils;
import com.ut.killer.execute.InstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class VmToolUtils {
    private static final Logger logger = LoggerFactory.getLogger(InstanceUtils.class);

    private static VmTool vmTool = null;

    public static String getLibName() {
        if (OSUtils.isLinux()) {
            return "libArthasJniLibrary-x64.so";
        }
        if (OSUtils.isWindows()) {
            return "libArthasJniLibrary-x64.dll";
        }
        return null;
    }

    public static String getLibPath() {
        return "jni/" + getLibName();
    }

    public static synchronized VmTool getVmToolInstance() {
        if (Objects.nonNull(vmTool)) {
            return vmTool;
        } else {
            String libPath = buildLipPath();
            vmTool = VmTool.getInstance(libPath);
        }
        return vmTool;
    }

    private static String buildLipPath() {
        String libPath = getLibPath();
        libPath = getLibPathFromTmpFile(libPath);
        return libPath;
    }

    private static String getLibPathFromTmpFile(String libPath) {
        // 尝试把lib文件复制到临时文件里，避免多次attach时出现 Native Library already loaded in another classloader
        FileOutputStream tmpLibOutputStream = null;
        InputStream libInputStream = null;
        try {
            File tmpLibFile = File.createTempFile(VmTool.JNI_LIBRARY_NAME, null);
            tmpLibOutputStream = new FileOutputStream(tmpLibFile);
            libInputStream = InstanceUtils.class.getClassLoader().getResourceAsStream(libPath);
            if (Objects.nonNull(libInputStream)) {
                IOUtils.copy(libInputStream, tmpLibOutputStream);
                libPath = tmpLibFile.getAbsolutePath();
                logger.debug("copy {} to {}", libPath, tmpLibFile);
            } else {
                logger.error("can not find lib: {}", libPath);
            }
        } catch (Throwable e) {
            logger.error("try to copy lib error! libPath: {}", libPath, e);
        } finally {
            IOUtils.close(libInputStream);
            IOUtils.close(tmpLibOutputStream);
        }
        return libPath;
    }
}