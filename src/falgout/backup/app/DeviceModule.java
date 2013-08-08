package falgout.backup.app;

import java.nio.file.FileStore;

import com.google.inject.AbstractModule;

public class DeviceModule extends AbstractModule {
    private final FileStore store;
    
    public DeviceModule(FileStore store) {
        this.store = store;
    }
    
    @Override
    protected void configure() {
        bind(FileStore.class).toInstance(store);
    }
}
