package falgout.backup;

import java.nio.file.FileStore;

public interface FileStoreListener {
	public void fileStoreAdded(FileStore store);
	
	public void fileStoreRemoved(FileStore store);
}
