package falgout.backup.app;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

/**
 * Stores information associated to a removable device via a UUID. This class
 * should be thread safe.
 * 
 * @author jeffrey
 * 
 */
public interface DeviceData {
    public UUID getID();
    
    public Set<Path> getPreviousRoots();
    
    public SortedSet<Path> getPathsToBackup();
    
    public Map<Path, Hash> getHashes();
}
