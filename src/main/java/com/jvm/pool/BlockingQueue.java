package com.jvm.pool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author heartccace
 * @create 2020-08-24 23:41
 * @Description TODO
 * @Version 1.0
 */
public class BlockingQueue<T> {
    private Deque<T> queue = new ArrayDeque<T>();

    private ReentrantLock lock = new ReentrantLock();

    private Condition fullWaitSet = lock.newCondition();

    private Condition emptyWaitSet = lock.newCondition();

    private int Capacity;

    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                emptyWaitSet.await();
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public T poll(Long timeout, TimeUnit unit) {
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                if(nanos <= 0) {
                    return null;
                }
                nanos = emptyWaitSet.awaitNanos(nanos);
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void put(T element) {
        lock.lock();
        try{
            while(queue.size() == Capacity) {
                fullWaitSet.await();
            }
            queue.addLast(element);
            emptyWaitSet.signal();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

class ThreadPool {

}