package falgout.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TemporaryFileStructure extends ExternalResource {
    private String prefix;
    public Path dir;
    public Path file1;
    public Path file2;
    public Path dir1;
    
    @Override
    public Statement apply(Statement base, Description description) {
        prefix = description.getTestClass().getSimpleName();
        return super.apply(base, description);
    }
    
    @Override
    protected void before() throws Throwable {
        dir = Files.createTempDirectory(prefix);
        dir1 = dir.resolve("dir1");
        file1 = dir.resolve("file1");
        file2 = dir1.resolve("file2");
        
        Files.createDirectory(dir1);
        Files.createFile(file1);
        Files.createFile(file2);
    }
    
    @Override
    protected void after() {
        try {
            Directories.delete(Directory.get(dir));
        } catch (NoSuchFileException e) {} catch (IOException e) {
            throw new Error(e);
        }
    }
}
