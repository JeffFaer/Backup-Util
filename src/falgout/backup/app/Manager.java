package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultManager.class)
public interface Manager {
    public void backup(FileStore store) throws IOException;
    
    public void restore(FileStore store) throws IOException;
    
    public void restore(FileStore store, Date date) throws IOException;
    
    public void restore(UUID id, Path dir) throws IOException;
    
    public void restore(UUID id, Path dir, Date date) throws IOException;
    
    public Set<UUID> getManagedDevices() throws IOException;
    
    public SortedSet<Date> getBackupDates(UUID id) throws IOException;
    
    public Map<UUID, SortedSet<Date>> getBackupDates() throws IOException;
    
    public boolean isConfigured(FileStore store) throws IOException;
}
