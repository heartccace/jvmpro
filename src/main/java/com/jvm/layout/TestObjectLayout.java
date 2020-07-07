package com.jvm.layout;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author heartccace
 * @create 2020-06-10 13:05
 * @Description TODO
 * @Version 1.0
 */
public class TestObjectLayout {
    /**
     * OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
     *       0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
     *       4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
     *       8     4        (object header)                           e5 01 00 20 (11100101 00000001 00000000 00100000) (536871397) 对象指针
     *      12     4        (loss due to the next object alignment)  对齐，必须是8的倍数
     * @param args
     */
    public static void main(String[] args) {
        Object o = new Object();
        System.out.println(ClassLayout.parseInstance(o).toPrintable());

    }
}
