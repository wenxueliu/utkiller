package com.ut.killer;

import com.ut.killer.agent.AgentUtils;
import com.ut.killer.spy.SpyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Main {
    private Thread shutdown;

    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            // check args
            if (args.length != 2
                    || StringUtils.isBlank(args[0])
                    || StringUtils.isBlank(args[1])) {
                throw new IllegalArgumentException("illegal args");
            }
            AgentUtils.start(args[0], args[1]);

        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("load jvm failed : " + t.getMessage());
            System.exit(-1);
        }
    }

    void start() {
        shutdown = new Thread("shutdown-hooker") {
            @Override
            public void run() {
                Main.this.destroy();
            }
        };
    }

    public void destroy() {
        SpyUtils.cleanUpSpyReference();
        if (Objects.nonNull(shutdown)) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdown);
            } catch (Throwable t) {
                // ignore
            }
        }
        Runtime.getRuntime().addShutdownHook(shutdown);
    }
}
