package com.ut.killer;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

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
            attachAgent(args[0], args[1], args[2]);
        } catch (Throwable t) {
            System.err.println("load jvm failed : " + t.getMessage());
            System.exit(-1);
        }
    }

    private static void attachAgent(final String mainClassPath,
                                    final String agentJarPath,
                                    final String cfg) throws Exception {

        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            System.out.println(descriptor.displayName());
            if (descriptor.displayName().equals(mainClassPath)) {
                attachPid(descriptor.id(), agentJarPath, cfg);
            }
        }
    }

    private static void attachPid(String targetJvmPid, String agentJarPath, String cfg) throws IOException {
        VirtualMachine vmObj = null;
        try {
            vmObj = VirtualMachine.attach(targetJvmPid);
            if (vmObj != null) {
                System.out.println(agentJarPath);
                vmObj.loadAgent(agentJarPath, cfg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (Objects.nonNull(vmObj)) {
                vmObj.detach();
            }
        }
    }
}
