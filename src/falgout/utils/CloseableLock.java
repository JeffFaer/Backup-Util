package falgout.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CloseableLock implements Lock, AutoCloseable {
    private final Lock l;
    
    private CloseableLock(Lock l) {
        this.l = l;
    }
    
    @Override
    public void lock() {
        l.lock();
    }
    
    @Override
    public void lockInterruptibly() throws InterruptedException {
        l.lockInterruptibly();
    }
    
    @Override
    public boolean tryLock() {
        return l.tryLock();
    }
    
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return l.tryLock(time, unit);
    }
    
    @Override
    public void unlock() {
        l.unlock();
    }
    
    @Override
    public Condition newCondition() {
        return l.newCondition();
    }
    
    @Override
    public void close() {
        l.unlock();
    }
    
    public static CloseableLock lock(Lock l) {
        l.lock();
        return createLock(l);
    }
    
    public static CloseableLock lockInterruptibly(Lock l) throws InterruptedException {
        l.lockInterruptibly();
        return createLock(l);
    }
    
    private static CloseableLock createLock(Lock l) {
        if (l instanceof CloseableLock) {
            return (CloseableLock) l;
        } else {
            return new CloseableLock(l);
        }
    }
}
