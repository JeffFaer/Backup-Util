package falgout.utils;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloseableLockTest {
	@Mock private Lock mock;
	
	@Test
	public void GettingAcquiresLock() {
		CloseableLock.lock(mock);
		verify(mock).lock();
	}
	
	@Test
	public void GettingInterruptiblyAcquiresLockInterruptibly() throws InterruptedException {
		CloseableLock.lockInterruptibly(mock);
		verify(mock).lockInterruptibly();
	}
	
	@Test
	public void GettingWithACloseableLockMaintainsIdentity() throws InterruptedException {
		CloseableLock l = CloseableLock.lock(mock);
		CloseableLock l2 = CloseableLock.lock(l);
		CloseableLock l3 = CloseableLock.lockInterruptibly(l);
		
		assertSame(l, l2);
		assertSame(l, l3);
	}
	
	@Test
	public void ClosingReleasesLock() {
		CloseableLock.lock(mock).close();
		verify(mock).unlock();
	}
	
	@Test
	public void DelegatesLockMethods() throws IllegalAccessException, InvocationTargetException, InterruptedException {
		for (Method m : Lock.class.getMethods()) {
			if (m.getParameterTypes().length == 0) {
				CloseableLock l = CloseableLock.lock(mock);
				reset(mock); // forget about mock.lock() from creation
				m.invoke(l);
				m.invoke(verify(mock));
			}
		}
		
		CloseableLock.lock(mock).tryLock(5, TimeUnit.DAYS);
		verify(mock).tryLock(5, TimeUnit.DAYS);
	}
	
	// Expected usage
	@Test
	public void UsingWithTryCatchAcquiresAndReleases() {
		try (CloseableLock l = CloseableLock.lock(mock)) {
			verify(mock).lock();
			verifyNoMoreInteractions(mock);
		}
		verify(mock).unlock();
	}
}
