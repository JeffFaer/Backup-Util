package falgout.backup.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import falgout.backup.AggregateFileStoreLocator;
import falgout.backup.FileStoreLocator;

public class Configuration {
	private static final String CONF_FILE = ".backupconf";
	
	private final FileStore store;
	private final Path root;
	private final UUID id;
	private final Set<Path> dirs = new TreeSet<>();
	
	private Configuration(FileStore store, Path root, UUID id, Collection<? extends Path> dirs) {
		this.store = store;
		this.root = root;
		this.id = id;
		this.dirs.addAll(dirs);
	}
	
	public FileStore getFileStore() {
		return store;
	}
	
	public Path getRoot() {
		return root;
	}
	
	public UUID getID() {
		return id;
	}
	
	public Set<Path> getDirectoriesToBackup() {
		return Collections.unmodifiableSet(dirs);
	}
	
	public void addDirectory(Path dir) throws IOException {
		Path rel;
		if (dir.isAbsolute()) {
			rel = root.relativize(dir);
		} else {
			rel = dir;
			dir = root.resolve(dir);
		}
		
		if (Files.notExists(dir)) {
			throw new NoSuchFileException(dir + " does not exist.");
		} else if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException(dir + " is not a directory.");
		} else if (!dir.startsWith(root)) { throw new IllegalArgumentException(dir + " is not a subdirectory of "
				+ root); }
		
		for (Path d : dirs) {
			if (rel.startsWith(d)) { return; }
		}
		
		Iterator<Path> i = dirs.iterator();
		while (i.hasNext()) {
			Path d = i.next();
			if (d.startsWith(rel)) {
				i.remove();
			}
		}
		
		dirs.add(rel);
	}
	
	public void removeDirectory(Path dir) {
		dirs.remove(dir);
	}
	
	public void save() throws IOException {
		try (BufferedWriter out = Files.newBufferedWriter(root.resolve(CONF_FILE), Charset.defaultCharset())) {
			out.write(id.toString());
			out.newLine();
			
			for (Path p : dirs) {
				out.write(p.toString());
				out.newLine();
			}
		}
	}
	
	public void delete() throws IOException {
		Files.deleteIfExists(root.resolve(CONF_FILE));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof Configuration)) { return false; }
		Configuration other = (Configuration) obj;
		if (id == null) {
			if (other.id != null) { return false; }
		} else if (!id.equals(other.id)) { return false; }
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BackupConfiguration [root=");
		builder.append(root);
		builder.append(", id=");
		builder.append(id);
		builder.append(", dirs=");
		builder.append(dirs);
		builder.append("]");
		return builder.toString();
	}
	
	public static Configuration load(FileStore store) throws IOException {
		return load(store, AggregateFileStoreLocator.getDefault());
	}
	
	public static Configuration load(FileStore store, FileStoreLocator locator) throws IOException {
		Path root = locator.getRootLocation(store);
		Path confFile = root.resolve(CONF_FILE);
		
		UUID id;
		List<Path> dirs = new ArrayList<>();
		if (Files.exists(confFile)) {
			List<String> lines = Files.readAllLines(confFile, Charset.defaultCharset());
			
			id = UUID.fromString(lines.get(0));
			
			for (int i = 1; i < lines.size(); i++) {
				dirs.add(Paths.get(lines.get(i)));
			}
		} else {
			id = UUID.randomUUID();
		}
		
		return new Configuration(store, root, id, dirs);
	}
}
