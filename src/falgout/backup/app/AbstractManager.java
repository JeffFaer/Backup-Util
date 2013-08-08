package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

import com.google.inject.Inject;

import falgout.backup.FileStoreLocator;

public abstract class AbstractManager implements Manager {
    private final FileStoreLocator locator;
    
    @Inject
    protected AbstractManager(FileStoreLocator locator) {
        this.locator = locator;
    }
    
    public FileStoreLocator getLocator() {
        return locator;
    }
    
    @Override
    public void backup(FileStore store) throws IOException {
        
    }
    
    protected abstract void doBackup(Device dev) throws IOException;
}
