package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.util.UUID;

public interface DeviceFactory {
    public Device create(FileStore store) throws IOException;
    
    public DeviceData create(UUID id) throws IOException;
}
