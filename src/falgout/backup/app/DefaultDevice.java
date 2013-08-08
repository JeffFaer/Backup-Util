package falgout.backup.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import falgout.backup.FileStoreIdentifier;
import falgout.backup.FileStoreLocator;

class DefaultDevice extends AbstractDevice {
    public static final Path DEFAULT_DIR = Paths.get(System.getProperty("user.home")).resolve(".backup-config");
    private final Path file;
    private final Set<Path> roots = new LinkedHashSet<>();
    private final Set<Path> pathsToBackup = new TreeSet<>();
    private final Map<Path, Hash> hashes = new LinkedHashMap<>();
    
    public DefaultDevice(FileStore store, FileStoreLocator l, FileStoreIdentifier i, Path dir) throws IOException {
        super(store, l, i);
        file = dir.resolve(getID().toString());
        
        load();
    }
    
    @Override
    public Set<Path> getPreviousRoots() {
        return Collections.unmodifiableSet(roots);
    }
    
    @Override
    public Set<Path> getPathsToBackup() {
        Set<Path> ret = new LinkedHashSet<>(pathsToBackup.size());
        for (Path p : pathsToBackup) {
            ret.add(getRoot().resolve(p).normalize());
        }
        return Collections.unmodifiableSet(ret);
    }
    
    @Override
    public Map<Path, Hash> getHashes() {
        return Collections.unmodifiableMap(hashes);
    }
    
    @Override
    protected boolean doUpdateHash(Path p, Hash hash) {
        Hash old = hashes.put(p, hash);
        return !Objects.equals(hash, old);
    }
    
    @Override
    protected boolean doAddPathToBackup(Path p) {
        Path rel = p;
        if (!p.isAbsolute()) {
            p = getRoot().resolve(p).normalize();
        }
        if (p.isAbsolute()) {
            rel = getRoot().relativize(p);
        }
        
        if (!p.startsWith(getRoot())) { throw new IllegalArgumentException(p + " is not a child of " + getRoot()); }
        if (Files.notExists(p)) { throw new IllegalArgumentException(p + " does not exist."); }
        
        // same as root
        if (Paths.get("").equals(rel)) {
            pathsToBackup.clear();
            return pathsToBackup.add(rel);
        }
        
        Iterator<Path> itr = pathsToBackup.iterator();
        while (itr.hasNext()) {
            Path path = itr.next();
            
            // same as root directory
            if (Paths.get("").equals(path)) { return false; }
            
            if (rel.startsWith(path)) {
                return false;
            } else if (path.startsWith(rel)) {
                itr.remove();
            }
        }
        
        return pathsToBackup.add(rel);
    }
    
    @Override
    protected boolean doRemovePathToBackup(Path p) {
        p = p.normalize();
        if (p.isAbsolute()) {
            p = getRoot().relativize(p);
        }
        return pathsToBackup.remove(p);
    }
    
    @Override
    protected void save() throws IOException {
        Set<String> roots = new LinkedHashSet<>(this.roots.size());
        Set<String> pathsToBackup = new LinkedHashSet<>(this.pathsToBackup.size());
        Map<String, Hash> hashes = new LinkedHashMap<>(this.hashes.size());
        
        for (Path p : this.roots) {
            roots.add(p.toString());
        }
        for (Path p : this.pathsToBackup) {
            pathsToBackup.add(p.toString());
        }
        for (Entry<Path, Hash> e : this.hashes.entrySet()) {
            hashes.put(e.getKey().toString(), e.getValue());
        }
        
        try (OutputStream out = Files.newOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(roots);
            oos.writeObject(pathsToBackup);
            oos.writeObject(hashes);
        }
    }
    
    private void load() throws IOException {
        if (Files.exists(file)) {
            Set<String> roots;
            Set<String> pathsToBackup;
            Map<String, Hash> hashes;
            
            try (InputStream in = Files.newInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(in)) {
                roots = (Set<String>) ois.readObject();
                pathsToBackup = (Set<String>) ois.readObject();
                hashes = (Map<String, Hash>) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new Error("Malformed device file for " + this + ".", e);
            }
            
            for (String s : roots) {
                this.roots.add(Paths.get(s));
            }
            for (String s : pathsToBackup) {
                this.pathsToBackup.add(Paths.get(s));
            }
            for (Entry<String, Hash> e : hashes.entrySet()) {
                this.hashes.put(Paths.get(e.getKey()), e.getValue());
            }
        }
        
        roots.add(getRoot());
    }
}
