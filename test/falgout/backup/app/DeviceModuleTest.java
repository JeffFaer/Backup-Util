package falgout.backup.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;

import falgout.backup.BackupModule;
import falgout.backup.MockModule;
import falgout.backup.TemporaryFileStore;
import falgout.backup.TemporaryFileStructure;

@RunWith(JukitoRunner.class)
public class DeviceModuleTest {
    @Rule @Inject public TemporaryFileStore fs;
    @Rule @Inject public TemporaryFileStructure config;
    @Inject private BackupModule backup;
    @Inject private DeviceModule device;
    private MockModule mock;
    
    @Before
    public void init() {
        mock = new MockModule(fs);
    }
    
    @Test
    public void CreatesDeviceWithProvidedStore() throws IOException {
        FileStore store = FileSystems.getDefault().getFileStores().iterator().next();
        when(fs.locator.getRootLocation(store)).thenReturn(Paths.get(""));
        
        DeviceFactory f = createFactory(backup, device, mock);
        Device d = f.create(store);
        
        assertSame(store, d.getFileStore());
    }
    
    private DeviceFactory createFactory(Module... modules) {
        return Guice.createInjector(modules).getInstance(DeviceFactory.class);
    }
    
    @Test
    public void CanCreateDeviceFromID() throws IOException {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("conf", config.dir.toString());
        
        DeviceFactory f = createFactory(new BackupModule(props), device, mock);
        Device d = f.create(fs.store);
        d.addPathToBackup(Paths.get("."));
        UUID id = d.getID();
        
        DeviceData d2 = f.create(id);
        
        assertEquals(d.getID(), d2.getID());
        Set<Path> relativized = new LinkedHashSet<>();
        for (Path p : d.getPathsToBackup()) {
            relativized.add(d.getRoot().relativize(p));
        }
        assertEquals(relativized, d2.getPathsToBackup());
        assertEquals(d.getPreviousRoots(), d2.getPreviousRoots());
        assertEquals(d.getHashes(), d2.getHashes());
    }
}
