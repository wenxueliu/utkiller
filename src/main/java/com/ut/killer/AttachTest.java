package com.ut.killer;


import com.sun.tools.attach.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class AttachTest {
    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
//        String mainClassPath = "com.imagedance.zpai.ZpaiApplication";
        String mainClassPath = "test.BaseTask";
        List<String> targetClassNames = Arrays.asList("test.BaseTask", "test.BaseTask1") ;
        String jarPath = "D:\\ut-killer-1.0.0.jar";

        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            System.out.println(descriptor.displayName());
            if (descriptor.displayName().equals(mainClassPath)) {
                VirtualMachine virtualMachine = VirtualMachine.attach(descriptor.id());
                for (String targetClassName : targetClassNames) {
                    virtualMachine.loadAgent(jarPath, targetClassName);
                }
                virtualMachine.detach();
            }
        }
    }
}