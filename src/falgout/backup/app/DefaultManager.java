package falgout.backup.app;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import falgout.backup.AggregateFileStoreLocator;
import falgout.backup.Directories;
import falgout.backup.Directory;
import falgout.backup.FileStoreLocator;

public class DefaultManager extends AbstractManager {
    private final Path backupRoot;
    private final MessageDigest md;
    
    public DefaultManager(Path backupRoot) throws NoSuchAlgorithmException {
        this(backupRoot, AggregateFileStoreLocator.getDefault());
    }
    
    public DefaultManager(Path backupRoot, FileStoreLocator locator) throws NoSuchAlgorithmException {
        this(backupRoot, locator, MessageDigest.getInstance("md5"));
    }
    
    public DefaultManager(Path backupRoot, FileStoreLocator locator, MessageDigest md) {
        super(locator);
        this.backupRoot = backupRoot;
        this.md = md;
    }
    
    public Path getBackupRoot() {
        return backupRoot;
    }
    
    public String getMessageDigestAlgorithm() {
        return md.getAlgorithm();
    }
    
    @Override
    protected void doBackup(Configuration conf, History history) throws IOException {
        List<Path> dirs = getDirectoriesToBackup(conf, history);
        if (dirs.isEmpty()) { return; }
        
        DateFormat format = new SimpleDateFormat("YYYYMMddHHmmssSSSS");
        Path backupDir = backupRoot.resolve(conf.getID().toString()).resolve(format.format(new Date()));
        Directory d = Directory.create(backupDir);
        
        for (Path dir : dirs) {
            Directories.copy(Directory.get(conf.getRoot().resolve(dir)), d);
        }
    }
    
    private List<Path> getDirectoriesToBackup(Configuration conf, History history) throws IOException {
        List<Path> dirs = new ArrayList<>();
        for (Path dir : conf.getDirectoriesToBackup()) {
            byte[] hash = Directories.digest(Directory.get(conf.getRoot().resolve(dir)), md);
            if (history.updateHash(dir, hash)) {
                dirs.add(dir);
            }
        }
        return dirs;
    }
}
