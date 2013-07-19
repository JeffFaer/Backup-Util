package falgout.backup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class FileStorePollerTest {
	private static final Object wait = new Object();
	
	@Mock private FileStoreListener listener;
	@Mock private FileSystem fileSystem;
	@Mock private FileStore store;
	private FileStorePoller poller;
	
	@Before
	public void init() {
		poller = new FileStorePoller(fileSystem, 50, TimeUnit.MILLISECONDS);
		poller.addListener(listener);
		
		when(fileSystem.getFileStores()).thenReturn(Collections.<FileStore> emptyList());
	}
	
	@Test(expected = IllegalStateException.class)
	public void FailsIfClosingBeforeStarting() {
		poller.close();
	}
	
	@Test(expected = IllegalStateException.class)
	public void FailsIfStartedTwice() {
		poller.start();
		poller.start();
	}
	
	@Test
	public void DetectsWhenFileStoreIsAdded() throws InterruptedException {
		when(fileSystem.getFileStores()).thenReturn(Collections.<FileStore> emptyList()).thenReturn(
				Arrays.asList(store));
		doAnswer(stop()).when(listener).fileStoreAdded(store);
		poller.start();
		
		synchronized (wait) {
			wait.wait(5000);
		}
		
		verify(listener).fileStoreAdded(store);
	}
	
	@Test
	public void DetectsWhenFileStoreIsRemoved() throws InterruptedException {
		when(fileSystem.getFileStores()).thenReturn(Arrays.asList(store)).thenReturn(
				Collections.<FileStore> emptyList());
		doAnswer(stop()).when(listener).fileStoreRemoved(store);
		poller.start();
		
		synchronized (wait) {
			wait.wait(5000);
		}
		
		verify(listener).fileStoreRemoved(store);
	}
	
	private Answer<Void> stop() {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				synchronized (wait) {
					wait.notifyAll();
				}
				
				poller.close();
				return null;
			}
		};
	}
	
	@Test
	public void CanDetectIfPollerIsRunning() {
		assertFalse(poller.isRunning());
		poller.start();
		assertTrue(poller.isRunning());
		poller.close();
		assertFalse(poller.isRunning());
	}
}