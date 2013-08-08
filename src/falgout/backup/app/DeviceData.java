package falgout.backup.app;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DeviceData {
    public UUID getID();
    
    public Set<Path> getPreviousRoots();
    
    public Set<Path> getPathsToBackup();
    
    public Map<Path, Hash> getHashes();
}
