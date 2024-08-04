package com.ut.killer;

import com.ut.killer.agent.AgentUtils;
import org.apache.commons.lang3.StringUtils;

public class Main {
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
}
