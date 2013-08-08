package falgout.backup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class FileStoreLocatorTest {
    private FileStoreLocator locator = new DefaultFileStoreLocator();
    
    @Test
    public void SanityCheck() throws IOException {
        Path cd = Paths.get(".").toRealPath();
        FileStore s = Files.getFileStore(cd);
        
        assertTrue(cd.startsWith(locator.getRootLocation(s)));
    }
}
