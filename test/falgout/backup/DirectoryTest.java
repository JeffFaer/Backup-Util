package falgout.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
public class DirectoryTest {
    @Rule @Inject public TemporaryFileStructure files;
    private Path nonExistant;
    
    @Before
    public void before() throws IOException {
        nonExistant = files.dir.resolve("IDontExist");
    }
    
    @Test(expected = IOException.class)
    public void GettingDirectoryMustExist() throws IOException {
        Directory.get(nonExistant);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void GettingDirectoryMustBeDirectory() throws IOException {
        Directory.get(files.file1);
    }
    
    @Test(expected = IOException.class)
    public void CreatingDirectoryMustNotBeFile() throws IOException {
        Directory.create(files.file1);
    }
    
    @Test
    public void CreatingDirectoryMakesNew() throws IOException {
        assertTrue(Files.notExists(nonExistant));
        Directory.create(nonExistant);
        assertTrue(Files.exists(nonExistant));
        Files.delete(nonExistant);
    }
    
    @Test
    public void IteratingIsRecursive() throws IOException {
        Directory d = Directory.get(files.dir);
        Set<Path> files2 = new LinkedHashSet<>();
        for (Path file : d.iterable(new Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isRegularFile(entry);
            }
        })) {
            files2.add(file);
        }
        
        assertEquals(new LinkedHashSet<>(Arrays.asList(files.file1, files.file2)), files2);
    }
    
    @Test
    public void IteratingOverRelativeDirectoryWorks() throws IOException {
        Directory d = Directory.get(".");
        Set<Path> files = new LinkedHashSet<>();
        for (Path p : d) {
            files.add(p);
        }
        
        assertTrue(files.contains(Paths.get("./LICENSE")));
        assertTrue(files.contains(Paths.get("./README.md")));
        assertTrue(files.contains(Paths.get("./src/falgout/backup/Directory.java")));
    }
}
