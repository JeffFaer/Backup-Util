package falgout.backup.app;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
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
        boolean changed = doUpdateHash(p, hash);
        if (changed) {
            doSave();
        }
        return changed;
    }
    
    protected abstract boolean doUpdateHash(Path p, Hash hash);
    
    @Override
    public boolean addPathToBackup(Path p) throws IOException {
        boolean changed = doAddPathToBackup(p);
        if (changed) {
            doSave();
        }
        return changed;
    }
    
    protected abstract boolean doAddPathToBackup(Path p);
    
    @Override
    public boolean removePathToBackup(Path p) throws IOException {
        boolean changed = doRemovePathToBackup(p);
        if (changed) {
            doSave();
        }
        return changed;
    }
    
    protected abstract boolean doRemovePathToBackup(Path p);
    
    private void doSave() throws IOException {
        save();
        
        if (setID) {
            i.setID(store, id);
            setID = false;
        }
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
