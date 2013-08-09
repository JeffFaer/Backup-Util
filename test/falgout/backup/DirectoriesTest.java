package falgout.backup;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Provides;

@RunWith(JukitoRunner.class)
public class DirectoriesTest {
    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {}
        
        @Provides
        TemporaryFileStructure create() {
            return new TemporaryFileStructure();
        }
    }
    
    @Rule @Inject public TemporaryFileStructure files;
    @Rule @Inject public TemporaryFileStructure files2;
    
    private Path dir1;
    private Path dir2;
    
    @Before
    public void before() throws IOException {
        dir1 = files.dir;
        dir2 = files2.dir;
        Directories.delete(dir2);
        Files.createDirectory(dir2);
    }
    
    @Test
    public void CopyTest() throws IOException {
        Directories.copy(dir1, dir2);
        assertTrue(Files.exists(files2.file1));
        assertTrue(Files.exists(files2.file2));
    }
    
    @Test
    public void StructureTest() throws IOException {
        assertFalse(Directories.isStructureSame(dir1, dir2));
        assertFalse(Directories.isStructureSame(dir2, dir1));
        Directories.copy(dir1, dir2);
        assertTrue(Directories.isStructureSame(dir1, dir2));
    }
    
    @Test
    public void DeleteTest() throws IOException {
        Directories.delete(dir1);
        assertTrue(Files.notExists(dir1));
    }
    
    @Test
    public void CanDeleteNonExistentDirectory() throws IOException {
        Directories.delete(dir1);
        Directories.delete(dir1);
    }
    
    @Test
    public void EnumerateTest() throws IOException {
        List<Path> l = Directories.enumerateEntries(dir1);
        assertThat(l, containsInAnyOrder(files.dir, files.file1, files.dir1, files.file2));
    }
    
    @Test
    public void DigestWorks() throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] md1 = Directories.digest(dir1, md5);
        byte[] md2 = Directories.digest(dir2, md5);
        
        assertFalse(Arrays.equals(md1, md2));
    }
    
    @Test
    public void AlteringFileInDirectoryAltersDigest() throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] md1 = Directories.digest(dir1, md5);
        Files.write(files.file1, new byte[] { 1, 2, 3, 4, 5 });
        assertFalse(Arrays.equals(md1, Directories.digest(dir1, md5)));
    }
}
