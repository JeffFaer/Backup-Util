package falgout.backup;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;

public interface FileStoreLocator {
	public Path getRootLocation(FileStore store) throws IOException;
}