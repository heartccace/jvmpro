package com.jvm.driver;

import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author heartccace
 * @create 2020-04-14 14:42
 * @Description TODO
 * @Version 1.0
 */
public class DriverTest {
    public static void main(String[] args) {
        ServiceLoader loader =ServiceLoader.load(Driver.class);
        Iterator iterator = loader.iterator();
        while(iterator.hasNext()) {
            System.out.println("ServiceLoader 加载的驱动: " + iterator.next());
        }
    }
}
