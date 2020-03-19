package com.jvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * @author heartccace
 * @create 2020-03-18 14:29
 * @Description 测试串行垃圾回收器SerialGC
 * @param 配置虚拟机参数：-XX:+UseSerialGC(使用串行垃圾回收) -XX:+PrintGCDetails -Xms8m -Xmx8m
 *               -XX:+UseParallelGC(使用并行垃圾回收)
 *               -XX:+UseParNew(使用并行垃圾回收)
 *               -XX:+UseConcMarkSweepGC（使用cms垃圾收集器）
 * @Version 1.0
 */
public class TestGC {
    private static List<Object> list = new ArrayList<Object>();
    public static void main(String[] args) {
        int time = new Random().nextInt(100);
        while (true) {
            if(time % 2 == 0) {
                list.clear();
            } else {
                for(int i = 0;i < 1000; i++) {
                    Properties pro = new Properties();
                    pro.put("key" + i, "value" + System.currentTimeMillis() + i);
                    list.add(pro);
                }
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
