package falgout.backup.app;

import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;

import org.jukito.JukitoRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import falgout.backup.BackupModule;
import falgout.backup.TemporaryFileStore;

@RunWith(JukitoRunner.class)
public class DeviceModuleTest {
    @Rule @Inject public TemporaryFileStore fs;
    @Inject private BackupModule backup;
    @Inject private DeviceModule device;
    
    @Test
    public void CreatesDeviceWithProvidedStore() throws IOException {
        FileStore store = FileSystems.getDefault().getFileStores().iterator().next();
        Injector i = Guice.createInjector(backup, device);
        DeviceFactory f = i.getInstance(DeviceFactory.class);
        Device d = f.create(store);
        
        assertSame(store, d.getFileStore());
    }
}
