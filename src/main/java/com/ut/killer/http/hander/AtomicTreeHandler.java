package com.ut.killer.http.hander;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.ut.killer.command.StringUtils;
import com.ut.killer.http.ResultData;
import com.ut.killer.http.TreeRequest;
import fi.iki.elonen.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AtomicTreeHandler extends JsonResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(AtomicTreeHandler.class);
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            TreeRequest treeRequest = handleRequest(session, TreeRequest.class);
            handle(treeRequest);
            return response(ResultData.success(treeRequest.getMethodRequest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(TreeRequest treeRequest) {
        String mainClassPath = treeRequest.getMainClassPath();
        List<String> targetClassNames = Arrays.asList("com.imagedance.zpai.controller.ImageController",
                "com.imagedance.zpai.service.ImageService",
                "javax.servlet.http.HttpServletRequest",
                "org.apache.catalina.connector.RequestFacade",
//                "com.imagedance.zpai.service.impl.ImageServiceImpl",
//                "com.imagedance.zpai.service.impl.ImageMetaServiceImpl",
                "com.imagedance.zpai.service.ImageService",
                "com.imagedance.zpai.service.ImageMetaService");
//        String jarPath = "D:\\ut-killer-1.0.0.jar";
        String jarPath = "/Users/liuwenxue/Documents/mycomputer/mygithub/utkiller/target/ut-killer-1.0.0-SNAPSHOT.jar";
//        AgentUtils.createJavaAgentJarFile(AgentMain.class);

        Optional<VirtualMachine> virtualMachineOptional = getVm(mainClassPath);
        if (!virtualMachineOptional.isPresent()) {
            logger.error("no VirtualMachine match");
            return;
        }
        virtualMachineOptional.ifPresent(virtualMachine -> {
            try {
                for (String targetClassName : targetClassNames) {
                    virtualMachine.loadAgent(jarPath, targetClassName);
                }
            } catch (Exception ex) {
                logger.error("no VirtualMachine match", ex);
            }
        });
    }

    public Optional<VirtualMachine> getVm(String mainClassPath) {
        if (StringUtils.isBlank(mainClassPath)) {
            return getVmSelf();
        }
        return getVmByMainClassPath(mainClassPath);
    }

    public Optional<VirtualMachine> getVmByMainClassPath(String mainClassPath) {
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            System.out.println(descriptor.displayName());
            if (descriptor.displayName().equals(mainClassPath)) {
                VirtualMachine virtualMachine = null;
                try {
                    virtualMachine = VirtualMachine.attach(descriptor.id());
                } catch (AttachNotSupportedException | IOException  ex) {
                    logger.error("VirtualMachine.attach error", ex);
                    return Optional.empty();
                }
                return Optional.of(virtualMachine);
            }
        }
        return Optional.empty();
    }

    public Optional<VirtualMachine> getVmSelf() {
        String nameOfRunningVm = ManagementFactory.getRuntimeMXBean().getName();
        String pid = nameOfRunningVm.substring(0, nameOfRunningVm.indexOf(64));
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(pid);
        } catch (AttachNotSupportedException | IOException  ex) {
            logger.error("VirtualMachine.attach error", ex);
            return Optional.empty();
        }
        return Optional.of(virtualMachine);
    }
}
