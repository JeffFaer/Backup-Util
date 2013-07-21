package falgout.backup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelativeDirectoriesTest {
    private Directory tmp1;
    private Directory tmp2;
    
    @Before
    public void before() throws IOException {
        tmp1 = Directory.get(Files.createDirectory(Paths.get("tmp1")));
        tmp2 = Directory.get(Files.createDirectory(Paths.get("tmp2")));
        
        Files.createFile(tmp1.getPath().resolve("foo"));
        Files.createDirectory(tmp1.getPath().resolve("bar"));
        Files.createFile(tmp1.getPath().resolve("bar/foo2"));
    }
    
    @After
    public void after() throws IOException {
        Directories.delete(tmp1);
        Directories.delete(tmp2);
    }
    
    @Test
    public void DeleteTest() throws IOException {
        Directories.delete(tmp1);
        assertTrue(Files.notExists(tmp1.getPath()));
    }
    
    @Test
    public void CopyTest() throws IOException {
        Directories.copy(tmp1, tmp2);
        assertTrue(Files.exists(tmp2.getPath().resolve("foo")));
        assertTrue(Files.exists(tmp2.getPath().resolve("bar/foo2")));
    }
    
    @Test
    public void StructureTest() throws IOException {
        assertFalse(Directories.isStructureSame(tmp1, tmp2));
        Directories.copy(tmp1, tmp2);
        assertTrue(Directories.isStructureSame(tmp1, tmp2));
    }
    
    @Test
    public void DigestWorks() throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] md1 = Directories.digest(tmp1, md5);
        byte[] md2 = Directories.digest(tmp2, md5);
        
        assertFalse(Arrays.equals(md1, md2));
    }
}
