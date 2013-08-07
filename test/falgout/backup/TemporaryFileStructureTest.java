package falgout.backup;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.jukito.JukitoRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
public class TemporaryFileStructureTest {
    @Rule @Inject public TemporaryFileStructure files;
    
    @Test
    public void CreatesFiles() {
        assertTrue(Files.exists(files.file1));
        assertTrue(Files.exists(files.file2));
    }
}
