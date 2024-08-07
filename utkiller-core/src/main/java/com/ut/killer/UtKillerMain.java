package com.ut.killer;

import com.ut.killer.agent.AgentUtils;
import org.apache.commons.lang3.StringUtils;

public class UtKillerMain {
    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        try {
            // check args
            if (args.length != 3
                    || StringUtils.isBlank(args[0])
                    || StringUtils.isBlank(args[1])
                    || StringUtils.isBlank(args[2])) {
                throw new IllegalArgumentException("illegal args");
            }
            args[2] = "port=8888";
            AgentUtils.attachAgent(args[0], args[1], args[2]);
        } catch (Throwable t) {
            System.err.println("load jvm failed : " + t.getMessage());
            System.exit(-1);
        }
    }
}
