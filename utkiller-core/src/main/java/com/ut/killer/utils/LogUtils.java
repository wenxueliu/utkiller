package com.ut.killer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogUtils {
    private static PrintStream ps = System.err;

    private static String logName = "utkiller";

    void initLog() {
        try {
            File logDir = new File(System.getProperty("user.home") + File.separator + "logs" + File.separator
                    + logName + File.separator);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            if (!logDir.exists()) {
                logDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "logs" + File.separator
                        + logName + File.separator);
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
            }
            File log = new File(logDir, logName + ".log");
            if (!log.exists()) {
                log.createNewFile();
            }
            ps = new PrintStream(new FileOutputStream(log, true));
        } catch (Throwable t) {
            t.printStackTrace(ps);
        }
    }

    public static void log(String msg) {
        ps.println(msg);
    }
}
