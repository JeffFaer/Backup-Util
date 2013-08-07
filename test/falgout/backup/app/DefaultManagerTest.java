package falgout.backup.app;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.backup.Directories;
import falgout.backup.Directory;
import falgout.backup.FileStoreLocator;
import falgout.backup.TemporaryFileStructure;

@RunWith(JukitoRunner.class)
public class DefaultManagerTest {
    @Rule @Inject public TemporaryFileStructure files;
    @Inject private FileStore store;
    private Path backupRoot;
    private DefaultManager m;
    
    @Before
    public void init(final FileStoreLocator locator) throws IOException, NoSuchAlgorithmException {
        backupRoot = Files.createTempDirectory(getClass().getSimpleName());
        when(locator.getRootLocation(store)).thenReturn(files.dir);
        
        Configuration conf = Configuration.load(store, locator);
        conf.addDirectory(Paths.get("."));
        conf.save();
        
        m = new DefaultManager(backupRoot, locator, MessageDigest.getInstance("md5"));
    }
    
    @After
    public void after() throws IOException {
        Directories.delete(Directory.get(backupRoot));
    }
    
    @Test
    public void BackingUpNewDeviceCreatesCopy() throws IOException {
        m.backup(store);
        
        assertTrue(Directories.isStructureSame(Directory.get(backupRoot), Directory.get(files.dir)));
    }
}
