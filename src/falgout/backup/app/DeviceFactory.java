package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

public interface DeviceFactory {
    public Device create(FileStore store) throws IOException;
}
