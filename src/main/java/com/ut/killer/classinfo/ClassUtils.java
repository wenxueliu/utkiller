package com.ut.killer.classinfo;

import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    public static List<ArgumentInfo> toArguments(Object[] args, String[] argNames) {
        List<ArgumentInfo> argumentInfos = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            ArgumentInfo argumentInfo = new ArgumentInfo();
            argumentInfo.setValue(args[i]);
            argumentInfo.setName(argNames[i]);
            argumentInfo.setType(args[i].getClass().getName());
            argumentInfos.add(argumentInfo);
        }
        return argumentInfos;
    }
}
