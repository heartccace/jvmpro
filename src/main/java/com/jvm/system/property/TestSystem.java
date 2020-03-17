package com.jvm.system.property;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

/**
 * @author liushuang
 * @create 2020-03-12 12:22
 */
public class TestSystem {
    // 通过java -D 设置系统参数
    //  java -Dstr="hello world"
    public static void main(String[] args) {
        String str = System.getProperty("str");
        if(str == null) {
            System.out.println("str is null");
        } else {
            System.out.print(str);
        }

    }
}
