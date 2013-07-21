package falgout.backup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
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
        try {
            poller.start();
            poller.start();
        } finally {
            poller.close();
        }
    }
    
    @Test
    public void DetectsWhenFileStoreIsAdded() throws InterruptedException {
        when(fileSystem.getFileStores()).thenReturn(Collections.<FileStore> emptyList()).thenReturn(
                Arrays.asList(store));
        doAnswer(stop()).when(listener).fileStoreAdded(store);
        poller.start();
        
        assertTrue(poller.awaitTermination(5, TimeUnit.SECONDS));
    }
    
    @Test
    public void DetectsWhenFileStoreIsRemoved() throws InterruptedException {
        when(fileSystem.getFileStores()).thenReturn(Arrays.asList(store)).thenReturn(
                Collections.<FileStore> emptyList());
        doAnswer(stop()).when(listener).fileStoreRemoved(store);
        poller.start();
        
        assertTrue(poller.awaitTermination(5, TimeUnit.SECONDS));
    }
    
    private Answer<Void> stop() {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
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
    
    @Test
    public void CanRestartPoller() {
        poller.start();
        poller.close();
        poller.start();
        poller.close();
    }
    
    @Test
    public void RestartedPollerWorksCorrectly() throws InterruptedException {
        poller.start();
        poller.close();
        DetectsWhenFileStoreIsAdded();
        DetectsWhenFileStoreIsRemoved();
    }
}
