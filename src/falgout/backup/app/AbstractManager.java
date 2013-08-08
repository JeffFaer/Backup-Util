package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

import com.google.inject.Inject;

public abstract class AbstractManager implements Manager {
    private final DeviceFactory factory;
    
    @Inject
    public AbstractManager(DeviceFactory factory) {
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
}
