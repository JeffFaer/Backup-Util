package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Manager {
    public void backup(FileStore store) throws IOException;
    
    public void restore(FileStore store) throws IOException;
    
    public void restore(FileStore store, Calendar date) throws IOException;
    
    public void restore(UUID id, Path dir) throws IOException;
    
    public void restore(UUID id, Path dir, Calendar date) throws IOException;
    
    public Set<UUID> getManagedDevices() throws IOException;
    
    public Set<Calendar> getBackupDates(UUID id) throws IOException;
    
    public Map<UUID, Set<Calendar>> getBackupDates() throws IOException;
}
