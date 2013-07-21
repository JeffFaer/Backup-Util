package falgout.backup;

import static falgout.backup.DefaultFileStoreLocator.LINUX;
import static falgout.backup.DefaultFileStoreLocator.MAC_OS;
import static falgout.backup.DefaultFileStoreLocator.WINDOWS;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import falgout.utils.OperatingSystem;

public class AggregateFileStoreLocator implements FileStoreLocator {
	private final Set<FileStoreLocator> locators = new CopyOnWriteArraySet<>();
	
	public AggregateFileStoreLocator() {}
	
	public AggregateFileStoreLocator(FileStoreLocator... locators) {
		this(Arrays.asList(locators));
	}
	
	public AggregateFileStoreLocator(Collection<? extends FileStoreLocator> locators) {
		this.locators.addAll(locators);
	}
	
	public void addFileStoreLocator(FileStoreLocator l) {
		locators.add(l);
	}
	
	public void removeFileStoreLocator(FileStoreLocator l) {
		locators.remove(l);
	}
	
	@Override
	public Path getRootLocation(FileStore store) throws IOException {
		for (FileStoreLocator l : locators) {
			Path p = l.getRootLocation(store);
			if (p != null) { return p; }
		}
		return null;
	}
	
	private static final AggregateFileStoreLocator DEFAULT = new AggregateFileStoreLocator();
	static {
		if (OperatingSystem.isWindows()) {
			DEFAULT.addFileStoreLocator(WINDOWS);
		} else if (OperatingSystem.isMac()) {
			DEFAULT.addFileStoreLocator(MAC_OS);
		} else {
			DEFAULT.addFileStoreLocator(LINUX);
		}
	}
	
	public static AggregateFileStoreLocator getDefault() {
		return DEFAULT;
	}
}
