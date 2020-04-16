package com.jvm.system.property;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author liushuang
 * @create 2020-03-12 12:22
 */
public class TestSystem {
    // 通过java -D 设置系统参数
    //  java -Dstr="hello world"
    // "sun.boot"
    public static void main(String[] args) {
        Properties properties = System.getProperties();
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entry: entries) {
            System.out.println(entry);

        }
        String str = System.getProperty("str");
        if(str == null) {
            System.out.println("str is null");
        } else {
            System.out.print(str);
        }

    }
}
