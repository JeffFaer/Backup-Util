package falgout.backup.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractDevice extends AbstractDeviceData implements Device {
    protected AbstractDevice() {}
    
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
        }
        
        return actuallySave;
    }
    
    protected abstract void save() throws IOException;
    
    @Override
    public abstract Path getRoot();
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" [getRoot()=");
        builder.append(getRoot());
        builder.append(", getID()=");
        builder.append(getID());
        builder.append("]");
        return builder.toString();
    }
}
