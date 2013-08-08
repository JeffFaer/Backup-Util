package falgout.backup.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.backup.FileStoreIdentifier;
import falgout.backup.TemporaryFileStore;
import falgout.backup.TemporaryFileStructure;

@RunWith(JukitoRunner.class)
public class DefaultDeviceTest {
    @Rule @Inject public TemporaryFileStructure files;
    @Rule @Inject public TemporaryFileStore fs;
    @Inject private FileStoreIdentifier i;
    private DefaultDevice dev;
    
    @Before
    public void init() throws IOException {
        dev = create();
    }
    
    private DefaultDevice create() throws IOException {
        return new DefaultDevice(fs.store, fs.locator, i, files.dir);
    }
    
    @Test
    public void KeepsLowestLevelDirectory() throws IOException {
        assertTrue(dev.addPathToBackup(fs.dir));
        assertFalse(dev.addPathToBackup(fs.dir1));
    }
    
    @Test
    public void RemovesHigherLevelDirectory() throws IOException {
        assertTrue(dev.addPathToBackup(fs.dir1));
        dev.addPathToBackup(fs.dir);
        assertEquals(Collections.singleton(fs.dir), dev.getPathsToBackup());
    }
    
    @Test
    public void RemovesFileIfParentDirectoryAdded() throws IOException {
        assertTrue(dev.addPathToBackup(fs.file2));
        dev.addPathToBackup(fs.dir);
        assertEquals(Collections.singleton(fs.dir), dev.getPathsToBackup());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void PathMustExist() throws IOException {
        dev.addPathToBackup(Paths.get("egrgergawg"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void PathMustBeChildOfRoot() throws IOException {
        dev.addPathToBackup(fs.dir.getParent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void RelativePathMustBeChildOfRoot() throws IOException {
        dev.addPathToBackup(Paths.get(".."));
    }
    
    @Test
    public void CanAddRelativeDirectory() throws IOException {
        assertTrue(dev.addPathToBackup(Paths.get(".")));
    }
    
    @Test
    public void AutmoaticallyAddsRootDirectory() {
        assertEquals(Collections.singleton(fs.dir), dev.getPreviousRoots());
    }
    
    @Test
    public void CanRemoveAbsolutePath() throws IOException {
        assertTrue(dev.addPathToBackup(fs.dir1));
        assertTrue(dev.removePathToBackup(fs.dir1));
    }
    
    @Test
    public void CanRemoveRelativePath() throws IOException {
        assertTrue(dev.addPathToBackup(fs.dir1));
        assertTrue(dev.removePathToBackup(dev.getRoot().relativize(fs.dir1)));
    }
    
    @Test
    public void CanRemoveRelativePathAsRoot() throws IOException {
        assertTrue(dev.addPathToBackup(Paths.get("")));
        assertTrue(dev.removePathToBackup(Paths.get(".")));
    }
    
    @Test
    public void AutomaticallySaves() throws IOException {
        Path file = files.dir.resolve(dev.getID().toString());
        
        assertTrue(Files.notExists(file));
        assertTrue(dev.addPathToBackup(Paths.get("")));
        assertTrue(Files.exists(file));
        
        verify(i).setID(fs.store, dev.getID());
    }
    
    @Test
    public void SavedInfoCarriesOver() throws IOException {
        when(i.getID(fs.store)).thenReturn(dev.getID());
        
        assertTrue(dev.addPathToBackup(Paths.get("")));
        
        Device dev2 = create();
        assertEquals(dev.getPathsToBackup(), dev2.getPathsToBackup());
        assertEquals(dev.getRoot(), dev2.getRoot());
        assertEquals(dev, dev2);
    }
}
