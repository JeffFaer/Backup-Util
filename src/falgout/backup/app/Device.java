package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Device {
    public FileStore getFileStore();
    
    public Path getRoot();
    
    public UUID getID();
    
    public Set<Path> getPreviousRoots();
    
    public Set<Path> getPathsToBackup();
    
    public Map<Path, Hash> getHashes();
    
    public boolean addPathToBackup(Path p) throws IOException;
    
    public boolean removePathToBackup(Path p) throws IOException;
    
    public boolean updateHash(Path p, byte[] hash) throws IOException;
    
    public boolean updateHash(Path p, Hash hash) throws IOException;
}
