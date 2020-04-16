package com.jvm.loader;

import com.jvm.DefaultClassLoader;

import java.lang.reflect.Method;

/**
 * @author heartccace
 * @create 2020-04-13 15:13
 * @Description TODO
 * @Version 1.0
 */
public class TestLoader {
    public static void main(String[] args) throws Exception{
        DefaultClassLoader classLoader1 = new DefaultClassLoader("classLoader1");
        DefaultClassLoader classLoader2 = new DefaultClassLoader("classLoader2");
        classLoader1.setPath("C:\\Users\\admin\\Desktop\\java\\");
        classLoader2.setPath("C:\\Users\\admin\\Desktop\\java\\");

        Class<?> clazz1 = classLoader1.loadClass("com.jvm.loader.FirstLoader");
        Class<?> clazz2 = classLoader2.loadClass("com.jvm.loader.FirstLoader");
        System.out.println(clazz1 == clazz2);

        System.out.println("clazz1 class loader: " + clazz1.getClassLoader());
        System.out.println("clazz2 class loader: " + clazz2.getClassLoader());

        Object object1 = clazz1.newInstance();
        Object object2= clazz2.newInstance();
        Method setLoader = clazz1.getDeclaredMethod("setLoader", Object.class);
        setLoader.invoke(object1,object2);
    }
}
