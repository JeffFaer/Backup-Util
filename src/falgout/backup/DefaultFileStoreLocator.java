package falgout.backup;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;

import com.google.inject.Singleton;

import falgout.utils.OperatingSystem;

@Singleton
public class DefaultFileStoreLocator implements FileStoreLocator {
    private static final FileStoreLocator INSTANCE;
    static {
        if (OperatingSystem.isWindows()) {
            INSTANCE = OSSpecificFileStoreLocator.WINDOWS;
        } else if (OperatingSystem.isMac()) {
            INSTANCE = OSSpecificFileStoreLocator.MAC_OS;
        } else {
            INSTANCE = OSSpecificFileStoreLocator.LINUX;
        }
    }
    
    @Override
    public Path getRootLocation(FileStore store) throws IOException {
        return INSTANCE.getRootLocation(store);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        return builder.toString();
    }
}
