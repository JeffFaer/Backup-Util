package falgout.backup.guice;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import falgout.backup.AggregateFileStoreLocator;
import falgout.backup.FileStoreLocator;

public class BackupModule extends AbstractModule {
    public static final Key<String> LOCATION = Key.get(String.class, Names.named("location"));
    
    private final Properties props;
    
    @Inject
    public BackupModule(Properties props) {
        this.props = props;
    }
    
    public BackupModule(Map<String, String> props) {
        this.props = new Properties();
        this.props.putAll(props);
    }
    
    public Properties getProperties() {
        return props;
    }
    
    @Override
    protected void configure() {
        Names.bindProperties(binder(), props);
        bind(String.class).annotatedWith(BackupLocation.class).to(LOCATION);
    }
    
    @Provides
    @BackupLocation
    @Singleton
    Path getLocation(@BackupLocation String location) {
        return Paths.get(location);
    }
    
    @Provides
    @Singleton
    FileStoreLocator getLocator() {
        return AggregateFileStoreLocator.getDefault();
    }
    
    @Provides
    MessageDigest getDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("md5");
    }
}
