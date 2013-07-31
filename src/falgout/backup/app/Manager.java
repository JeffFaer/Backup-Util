package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultManager.class)
public interface Manager {
    public void backup(FileStore store) throws IOException;
}
