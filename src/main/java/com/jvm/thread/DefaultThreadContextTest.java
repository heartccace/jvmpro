package com.jvm.thread;

/**
 * @author heartccace
 * @create 2020-04-14 12:07
 * @Description 当前类加载器(current class loader)
 * 每个类都会使用自己的类加载器（即加载自身类加载器）来去加载其它类，
 * 如果classx引用了classy，那么classx的类加载器就回去加载classy（前提classy未被加载）
 * @Version 1.0
 */
public class DefaultThreadContextTest {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getContextClassLoader());
        System.out.println(Thread.class.getClassLoader());
    }
}
