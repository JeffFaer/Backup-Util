package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;

import com.google.inject.Guice;

import falgout.backup.AggregateFileStoreLocator;
import falgout.backup.FileStoreLocator;
import falgout.backup.guice.DeviceModule;

public abstract class AbstractManager implements Manager {
    private final FileStoreLocator locator;
    
    protected AbstractManager() {
        this(AggregateFileStoreLocator.getDefault());
    }
    
    protected AbstractManager(FileStoreLocator locator) {
        this.locator = locator;
    }
    
    public FileStoreLocator getLocator() {
        return locator;
    }
    
    @Override
    public void backup(FileStore store) throws IOException {
        Configuration conf = Configuration.load(store, locator);
        History history = Guice.createInjector(new DeviceModule(conf)).getInstance(DeviceModule.HISTORY_PROVIDER).get();
        
        doBackup(conf, history);
    }
    
    protected abstract void doBackup(Configuration conf, History history) throws IOException;
    
    public static void main(String[] args) throws IOException {
        new AbstractManager() {
            @Override
            protected void doBackup(Configuration conf, History history) throws IOException {
                System.out.println(conf);
                System.out.println(history);
            }
        }.backup(FileSystems.getDefault().getFileStores().iterator().next());
    }
}
