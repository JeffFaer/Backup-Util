package falgout.backup.guice;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

public class PathProvider implements Provider<Path> {
    private final Provider<String> provider;
    
    @Inject
    public PathProvider(Provider<String> provider) {
        this.provider = provider;
    }
    
    @Singleton
    @Override
    public Path get() {
        return Paths.get(provider.get());
    }
}
