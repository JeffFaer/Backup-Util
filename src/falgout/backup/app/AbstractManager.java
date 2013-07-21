package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;

import falgout.backup.AggregateFileStoreLocator;
import falgout.backup.FileStoreLocator;

public abstract class AbstractManager implements Manager {
	private final FileStoreLocator locator;
	
	protected AbstractManager() {
		this(AggregateFileStoreLocator.getDefault());
	}
	
	protected AbstractManager(FileStoreLocator locator) {
		this.locator = locator;
	}
	
	public FileStoreLocator getLocator() {
		return locator;
	}
	
	@Override
	public void backup(FileStore store) throws IOException {
		Configuration conf = Configuration.load(store, locator);
		History history = History.get(conf.getID());
		history.addAlias(conf.getRoot());
		
		doBackup(conf, history);
	}
	
	protected abstract void doBackup(Configuration conf, History history) throws IOException;
}
