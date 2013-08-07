package falgout.backup.app;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import falgout.backup.Directories;
import falgout.backup.Directory;
import falgout.backup.FileStoreLocator;
import falgout.backup.guice.BackupLocation;

public class DefaultManager extends AbstractManager {
    private static final Filter<Path> FILES = new Filter<Path>() {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isRegularFile(entry);
        }
    };
    private final Path backupLocation;
    private final MessageDigest md;
    
    @Inject
    public DefaultManager(@BackupLocation Path backupLocation, FileStoreLocator locator, MessageDigest md) {
        super(locator);
        this.backupLocation = backupLocation;
        this.md = md;
    }
    
    @Override
    protected void doBackup(Configuration conf, History history) throws IOException {
        List<Path> files = getFilesToBackup(conf, history);
        System.out.println(conf);
        System.out.println(history);
        System.out.println(files);
    }
    
    private List<Path> getFilesToBackup(Configuration conf, History history) throws IOException {
        List<Path> backup = new ArrayList<>();
        for (Path dir : conf.getDirectoriesToBackup()) {
            Path resolvedDir = conf.getRoot().resolve(dir);
            for (Path file : Directory.get(resolvedDir).iterable(FILES)) {
                Path resolved = resolvedDir.resolve(file);
                byte[] hash = Directories.digest(Files.newInputStream(resolved), md);
                if (history.updateHash(resolvedDir.relativize(resolved), hash)) {
                    backup.add(file);
                }
            }
        }
        
        return backup;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DefaultManager [backupLocation=");
        builder.append(backupLocation);
        builder.append(", md=");
        builder.append(md);
        builder.append(", getLocator()=");
        builder.append(getLocator());
        builder.append("]");
        return builder.toString();
    }
}
