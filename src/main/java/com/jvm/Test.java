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
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "root");
        System.out.println(connection);
    }
}
