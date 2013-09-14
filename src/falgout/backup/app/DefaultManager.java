package falgout.backup.app;

import static falgout.backup.Directories.NO_OPTIONS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.google.inject.Inject;

import falgout.backup.Directories;
import falgout.backup.guice.BackupLocation;

class DefaultManager extends AbstractManager {
    private static final ThreadLocal<DateFormat> FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmssSSS");
        }
    };
    
    private final Path backupRoot;
    private final MessageDigest md;
    
    @Inject
    public DefaultManager(DeviceFactory factory, @BackupLocation Path backupRoot, MessageDigest md) {
        super(factory);
        this.backupRoot = backupRoot;
        this.md = md;
    }
    
    @Override
    public Set<UUID> getManagedDevices() throws IOException {
        List<Path> paths = Directories.enumerateEntries(backupRoot, NO_OPTIONS, 1);
        Set<UUID> ids = new LinkedHashSet<>(paths.size());
        for (Path p : paths) {
            if (Files.isDirectory(p)) {
                try {
                    ids.add(UUID.fromString(p.getFileName().toString()));
                } catch (IllegalArgumentException e) {
                    // not a UUID
                }
            }
        }
        
        return ids;
    }
    
    @Override
    public SortedSet<Date> getBackupDates(UUID id) throws IOException {
        Path parentDir = backupRoot.resolve(id.toString());
        SortedSet<Date> times = new TreeSet<>();
        
        if (Files.exists(parentDir)) {
            DateFormat f = FORMAT.get();
            for (Path p : Directories.enumerateEntries(parentDir, NO_OPTIONS, 1)) {
                if (Files.isDirectory(p)) {
                    try {
                        Date d = f.parse(p.getFileName().toString());
                        times.add(d);
                    } catch (ParseException e) {
                        // not a Date
                    }
                }
            }
        }
        
        return times;
    }
    
    @Override
    protected void doBackup(Device dev) throws IOException {
        Path previousBackup = getRestoreDir(dev, new Date());
        Path dir = getNewBackupDir(dev);
        Files.createDirectories(dir);
        
        for (Entry<Path, Boolean> e : updateHashes(dev).entrySet()) {
            Path file = e.getKey();
            Path rel = dev.getRoot().relativize(file);
            Path newFile = dir.resolve(rel);
            Files.createDirectories(newFile.getParent());
            
            if (e.getValue()) {
                Files.copy(file, newFile);
            } else {
                Files.createLink(newFile, previousBackup.resolve(rel));
            }
        }
    }
    
    private Map<Path, Boolean> updateHashes(Device dev) throws IOException {
        Map<Path, byte[]> hashes = new LinkedHashMap<>();
        
        for (Path f : getFilesToBackup(dev)) {
            hashes.put(f, Directories.digest(f, md));
        }
        
        return dev.updateHashes(hashes);
    }
    
    private Set<Path> getFilesToBackup(Device dev) throws IOException {
        Set<Path> files = new LinkedHashSet<>();
        for (Path p : dev.getPathsToBackup()) {
            if (Files.isRegularFile(p)) {
                files.add(p);
            } else {
                for (Path p2 : Directories.enumerateEntries(p)) {
                    if (Files.isRegularFile(p2)) {
                        files.add(p2);
                    }
                }
            }
        }
        return files;
    }
    
    Path getBackupRoot(DeviceData dev) {
        return backupRoot.resolve(dev.getID().toString());
    }
    
    Path getNewBackupDir(DeviceData dev) {
        Path dir;
        do {
            Date d = new Date();
            dir = getBackupDir(dev, d);
        } while (Files.exists(dir));
        return dir;
    }
    
    Path getBackupDir(DeviceData dev, Date date) {
        Path root = getBackupRoot(dev);
        return root.resolve(FORMAT.get().format(date));
    }
    
    Date getRestoreDate(DeviceData dev, Date date) throws IOException {
        SortedSet<Date> dates = getBackupDates(dev.getID());
        if (dates.isEmpty()) { return null; }
        
        return dates.contains(date) ? date : dates.headSet(date).last();
    }
    
    Path getRestoreDir(DeviceData dev, Date date) throws IOException {
        Date d = getRestoreDate(dev, date);
        return d == null ? null : getBackupDir(dev, d);
    }
    
    @Override
    protected void doRestore(DeviceData data, Path dir, Date date) throws IOException {
        Path restoreDir = getRestoreDir(data, date);
        if (restoreDir == null) { throw new IllegalStateException("No backups for " + data.getID()); }
        Directories.copy(restoreDir, dir, StandardCopyOption.REPLACE_EXISTING);
    }
}
