package falgout.backup;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

import falgout.backup.guice.BackupLocation;
import falgout.backup.guice.ConfigurationDirectory;
import falgout.backup.guice.IdentificationFile;
import falgout.backup.guice.PathProvider;

public class BackupModule extends AbstractModule {
    public static final Path DEFAULT_ID_FILE = Paths.get(".dev-id");
    public static final Path DEFAULT_BACKUP_DIR = Paths.get(System.getProperty("user.home")).resolve(".backup");
    public static final Path DEFAULT_CONF_DIR = DEFAULT_BACKUP_DIR.resolve(".conf");
    
    public static final Map<String, String> DEFAULT_PROPERTIES;
    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", DEFAULT_ID_FILE.toString());
        map.put("conf", DEFAULT_CONF_DIR.toString());
        map.put("location", DEFAULT_BACKUP_DIR.toString());
        
        DEFAULT_PROPERTIES = Collections.unmodifiableMap(map);
    }
    
    private final Map<String, String> props;
    
    public BackupModule() {
        this(Collections.EMPTY_MAP);
    }
    
    public BackupModule(Map<String, String> props) {
        props = new LinkedHashMap<>(props);
        for (String key : DEFAULT_PROPERTIES.keySet()) {
            if (!props.containsKey(key)) {
                props.put(key, DEFAULT_PROPERTIES.get(key));
            }
        }
        
        this.props = props;
    }
    
    @Override
    protected void configure() {
        Names.bindProperties(binder(), props);
        
        bindPath("id", IdentificationFile.class);
        bindPath("conf", ConfigurationDirectory.class);
        bindPath("location", BackupLocation.class);
    }
    
    private void bindPath(String name, Class<? extends Annotation> clazz) {
        Key<String> prop = Key.get(String.class, Names.named(name));
        bind(String.class).annotatedWith(clazz).to(prop);
        bind(Path.class).annotatedWith(clazz).toProvider(new PathProvider(getProvider(prop)));
    }
}
