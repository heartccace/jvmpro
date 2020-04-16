package com.jvm;

import java.io.*;

/**
 * @author heartccace
 * @create 2020-04-13 11:54
 * @Description custom class loader
 * @Version 1.0
 */
public class DefaultClassLoader extends ClassLoader{
    private String classLoaderName;
    private String path;
    private String fileExtension = ".class";

    public void setPath(String path) {
        this.path = path;
    }

    public DefaultClassLoader(String classLoaderName) {
        super();
        this.classLoaderName = classLoaderName;
    }

    protected DefaultClassLoader(String classLoaderName, ClassLoader parent) {
        super(parent);
        this.classLoaderName = classLoaderName;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        byte[] data = this.loadClassData(className);
        return this.defineClass(className,data, 0, data.length);
    }

    public byte[] loadClassData(String className) {
        InputStream in = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        className = className.replace(".","\\");
        try{
            in = new FileInputStream(new File(this.path + className + this.fileExtension));
            baos = new ByteArrayOutputStream();
            int ch;
            while( -1 != (ch = in.read())) {
                baos.write(ch);
            }
            data = baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        DefaultClassLoader classLoader = new DefaultClassLoader("default");
        classLoader.setPath("C:\\Users\\admin\\Desktop\\java\\");
        Class<?> clazz = classLoader.loadClass("Test");
        Object o = clazz.newInstance();
        System.out.println(o.getClass().getClassLoader());

    }
}
