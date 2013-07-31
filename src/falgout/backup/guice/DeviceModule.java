package falgout.backup.guice;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.UUID;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.throwingproviders.CheckedProvides;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

import falgout.backup.app.Configuration;
import falgout.backup.app.History;

public class DeviceModule extends AbstractModule {
    public static final Key<IOProvider<History>> HISTORY_PROVIDER = Key.get(new TypeLiteral<IOProvider<History>>() {});
    private final Configuration conf;
    
    public DeviceModule(Configuration conf) {
        this.conf = conf;
    }
    
    @Override
    protected void configure() {
        bind(UUID.class).toInstance(conf.getID());
        bind(FileStore.class).toInstance(conf.getFileStore());
        bind(Path.class).toInstance(conf.getRoot());
        
        install(ThrowingProviderBinder.forModule(this));
    }
    
    @CheckedProvides(IOProvider.class)
    @Singleton
    History getHistory() throws IOException {
        History h = History.get(conf.getID());
        h.addAlias(conf.getRoot());
        return h;
    }
}
