package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractManager implements Manager {
    private final DeviceFactory factory;
    
    protected AbstractManager(DeviceFactory factory) {
        this.factory = factory;
    }
    
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
        restore(store, Calendar.getInstance());
    }
    
    @Override
    public void restore(FileStore store, Calendar date) throws IOException {
        Device dev = factory.create(store);
        doRestore(dev, dev.getRoot(), date);
    }
    
    @Override
    public void restore(UUID id, Path dir) throws IOException {
        restore(id, dir, Calendar.getInstance());
    }
    
    @Override
    public void restore(UUID id, Path dir, Calendar date) throws IOException {
        doRestore(factory.create(id), dir, date);
    }
    
    protected abstract void doRestore(DeviceData data, Path dir, Calendar date) throws IOException;
    
    @Override
    public Map<UUID, Set<Calendar>> getBackupDates() throws IOException {
        Map<UUID, Set<Calendar>> dates = new LinkedHashMap<>();
        for (UUID id : getManagedDevices()) {
            dates.put(id, getBackupDates(id));
        }
        return dates;
    }
}
