package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import falgout.backup.FileStoreIdentifier;
import falgout.backup.FileStoreLocator;

public abstract class AbstractDevice implements Device {
    private final FileStore store;
    private final Path root;
    
    private final UUID id;
    private boolean setID;
    private final FileStoreIdentifier i;
    
    protected AbstractDevice(FileStore store, FileStoreLocator l, FileStoreIdentifier i) throws IOException {
        this.store = store;
        root = l.getRootLocation(store);
        
        UUID uid = i.getID(store);
        setID = uid == null;
        id = setID ? UUID.randomUUID() : uid;
        this.i = i;
    }
    
    @Override
    public FileStore getFileStore() {
        return store;
    }
    
    @Override
    public Path getRoot() {
        return root;
    }
    
    @Override
    public UUID getID() {
        return id;
    }
    
    @Override
    public boolean updateHash(Path p, byte[] hash) throws IOException {
        return updateHash(p, new Hash(hash));
    }
    
    @Override
    public boolean updateHash(Path p, Hash hash) throws IOException {
        return doSave(doUpdateHash(p, hash));
    }
    
    protected abstract boolean doUpdateHash(Path p, Hash hash);
    
    @Override
    public Map<Path, Boolean> updateHashes(Map<Path, byte[]> hashes) throws IOException {
        boolean changed = false;
        Map<Path, Boolean> results = new LinkedHashMap<>(hashes.size());
        for (Entry<Path, byte[]> e : hashes.entrySet()) {
            boolean r = doUpdateHash(e.getKey(), new Hash(e.getValue()));
            changed = changed || r;
            results.put(e.getKey(), r);
        }
        doSave(changed);
        return results;
    }
    
    @Override
    public boolean addPathToBackup(Path p) throws IOException {
        return doSave(doAddPathToBackup(p));
    }
    
    protected abstract boolean doAddPathToBackup(Path p);
    
    @Override
    public Map<Path, Boolean> addPathsToBackup(Collection<? extends Path> paths) throws IOException {
        boolean changed = false;
        Map<Path, Boolean> results = new LinkedHashMap<>(paths.size());
        for (Path p : paths) {
            boolean r = doAddPathToBackup(p);
            changed = changed || r;
            results.put(p, r);
        }
        doSave(changed);
        return results;
    }
    
    @Override
    public boolean removePathToBackup(Path p) throws IOException {
        return doSave(doRemovePathToBackup(p));
    }
    
    protected abstract boolean doRemovePathToBackup(Path p);
    
    @Override
    public Map<Path, Boolean> removePathsToBackup(Collection<? extends Path> paths) throws IOException {
        boolean changed = false;
        Map<Path, Boolean> results = new LinkedHashMap<>(paths.size());
        for (Path p : paths) {
            boolean r = doRemovePathToBackup(p);
            changed = changed || r;
            results.put(p, r);
        }
        doSave(changed);
        return results;
    }
    
    private boolean doSave(boolean actuallySave) throws IOException {
        if (actuallySave) {
            save();
            
            if (setID) {
                i.setID(store, id);
                setID = false;
            }
        }
        
        return actuallySave;
    }
    
    protected abstract void save() throws IOException;
    
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
        if (!(obj instanceof AbstractDevice)) { return false; }
        AbstractDevice other = (AbstractDevice) obj;
        if (id == null) {
            if (other.id != null) { return false; }
        } else if (!id.equals(other.id)) { return false; }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" [root=");
        builder.append(root);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}
