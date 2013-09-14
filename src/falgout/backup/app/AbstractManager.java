package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

public abstract class AbstractManager implements Manager {
    private final DeviceFactory factory;
    
    protected AbstractManager(DeviceFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public DeviceFactory getFactory() {
        return factory;
    }
    
    @Override
    public void backup(FileStore store) throws IOException {
        doBackup(factory.create(store));
    }
    
    protected abstract void doBackup(Device dev) throws IOException;
    
    @Override
    public void restore(FileStore store) throws IOException {
        restore(store, new Date());
    }
    
    @Override
    public void restore(FileStore store, Date date) throws IOException {
        Device dev = factory.create(store);
        doRestore(dev, dev.getRoot(), date);
    }
    
    @Override
    public void restore(UUID id, Path dir) throws IOException {
        restore(id, dir, new Date());
    }
    
    @Override
    public void restore(UUID id, Path dir, Date date) throws IOException {
        doRestore(factory.create(id), dir, date);
    }
    
    protected abstract void doRestore(DeviceData data, Path dir, Date date) throws IOException;
    
    @Override
    public Map<UUID, SortedSet<Date>> getBackupDates() throws IOException {
        Map<UUID, SortedSet<Date>> dates = new LinkedHashMap<>();
        for (UUID id : getManagedDevices()) {
            dates.put(id, getBackupDates(id));
        }
        return dates;
    }
    
    @Override
    public boolean isConfigured(FileStore store) throws IOException {
        Device d = factory.create(store);
        return getManagedDevices().contains(d.getID());
    }
}
