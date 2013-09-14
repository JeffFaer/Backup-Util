package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

public interface Device extends DeviceData {
    public FileStore getFileStore();
    
    public Path getRoot();
    
    /**
     * Returns the {@link DeviceData#getPathsToBackup() paths to backup}
     * resolved
     * against the {@link #getRoot() root}.
     */
    @Override
    public SortedSet<Path> getPathsToBackup();
    
    public boolean addPathToBackup(Path p) throws IOException;
    
    public Map<Path, Boolean> addPathsToBackup(Collection<? extends Path> paths) throws IOException;
    
    public boolean removePathToBackup(Path p) throws IOException;
    
    public Map<Path, Boolean> removePathsToBackup(Collection<? extends Path> paths) throws IOException;
    
    public boolean updateHash(Path p, byte[] hash) throws IOException;
    
    public boolean updateHash(Path p, Hash hash) throws IOException;
    
    public Map<Path, Boolean> updateHashes(Map<Path, byte[]> hashes) throws IOException;
}
