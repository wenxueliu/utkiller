package com.ut.killer.bytekit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransformerManager {
    private Instrumentation instrumentation;

    private List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<>();

    private ClassFileTransformer transformer;


    private static TransformerManager transformerManager;

    public static TransformerManager getInstance(Instrumentation instrumentation) {
        if (transformerManager == null) {
            transformerManager = new TransformerManager(instrumentation);
        }
        return transformerManager;
    }

    public TransformerManager(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public void addTransformer(ClassFileTransformer transformer) {
        this.transformer = transformer;
    }

    public void removeTransformer() {
        this.instrumentation.removeTransformer(transformer);
    }

    public void destroy() {
        this.instrumentation.removeTransformer(transformer);
//        transformers.clear();
    }
}