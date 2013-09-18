package falgout.backup.app.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import javax.swing.AbstractListModel;

import falgout.backup.app.Device;

public class BackupPathModel extends AbstractListModel<Path> {
    private static final long serialVersionUID = -1733574594849537248L;
    private final Device dev;
    
    public BackupPathModel(Device dev) {
        this.dev = dev;
    }
    
    public Device getDev() {
        return dev;
    }
    
    @Override
    public int getSize() {
        return dev.getPathsToBackup().size();
    }
    
    @Override
    public Path getElementAt(int index) {
        if (index >= getSize()) { throw new IndexOutOfBoundsException(index + " >= " + getSize()); }
        
        Iterator<Path> i = dev.getPathsToBackup().iterator();
        while (--index >= 0) {
            i.next();
        }
        return i.next();
    }
    
    public boolean add(Path p) throws IOException {
        if (dev.addPathToBackup(p)) {
            int i = indexOf(p);
            fireIntervalAdded(this, i, i);
            return true;
        }
        
        return false;
    }
    
    public boolean remove(Path p) throws IOException {
        int i = indexOf(p);
        if (dev.removePathToBackup(p)) {
            fireIntervalRemoved(this, i, i);
            return true;
        }
        
        return false;
    }
    
    private int indexOf(Path p) {
        Iterator<Path> itr = dev.getPathsToBackup().iterator();
        int i = 0;
        while (itr.hasNext()) {
            if (itr.next().equals(p)) { return i; }
            i++;
        }
        return -1;
    }
}
