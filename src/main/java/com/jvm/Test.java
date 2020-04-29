package com.jvm;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author heartccace
 * @create 2020-04-14 15:41
 * @Description TODO
 * @Version 1.0
 */
public class Test {

    private static int i; // 0

    private static int j = 1; // 0

    private static Test tst= new Test(); // null -> i -> 1 j->1

    private Test() {
        i++;
        j++;
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        int i =1;
        i =  i ++; // 本地变量表                1   2   3
        System.out.println(i); // 1            3
        int j = i ++;          //                  1
        int k = i+ ++i * i++;
        System.out.println(i); // 4
        System.out.println(j); // 1
        System.out.println(k);
//        Class.forName("com.mysql.jdbc.Driver");
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "root");
//        System.out.println(connection);
    }
}
