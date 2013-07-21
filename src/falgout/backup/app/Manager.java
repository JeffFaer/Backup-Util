package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

public interface Manager {
    public void backup(FileStore store) throws IOException;
}
