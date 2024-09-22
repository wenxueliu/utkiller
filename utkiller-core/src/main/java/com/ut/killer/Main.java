package com.ut.killer;

import com.ut.killer.agent.AgentUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main 类作为应用程序的主要入口点
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(AgentUtils.class);

    /**
     * 应用程序的主入口点
     *
     * @param args 命令行参数数组，预期至少包含两个非空字符串参数。
     */
    public static void main(String[] args) {
        logger.debug("args[0]={},args[1]={}", args[0], args[1]);
        try {
            if (args.length != 2
                    || StringUtils.isBlank(args[0])
                    || StringUtils.isBlank(args[1])) {
                throw new IllegalArgumentException("illegal args, refer to README");
            }
            AgentUtils.start(args[0], args[1]);
        } catch (Exception ex) {
            logger.error("load jvm failed", ex);
        }
    }
}
