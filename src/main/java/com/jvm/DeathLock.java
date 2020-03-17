package com.jvm;

/**
 * @author liushuang
 * @create 2020-03-17 14:31
 */
public class DeathLock {
    private  static Object obj1 = new Object();
    private static Object obj2 = new Object();

    public static void main(String[] args) {
       new Thread(new Thread1()).start();
        new Thread(new Thread2()).start();
    }

    private static class Thread1 implements Runnable {

        public void run() {
            synchronized (obj1) {
                try {
                    System.out.println("will sleep 2 seconds");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj2) {
                    System.out.println("block 2");
                }
            }
        }
    }


    private static class Thread2 implements Runnable {

        public void run() {
            synchronized (obj2) {
                try {
                    System.out.println("will sleep 2 seconds");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj1) {
                    System.out.println("block 2");
                }
            }
        }
    }

}
