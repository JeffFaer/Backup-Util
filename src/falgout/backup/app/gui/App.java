package falgout.backup.app.gui;

import java.io.IOException;
import java.nio.file.FileStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import falgout.backup.BackupModule;
import falgout.backup.FileStoreListener;
import falgout.backup.FileStorePoller;
import falgout.backup.app.Device;
import falgout.backup.app.DeviceFactory;
import falgout.backup.app.DeviceModule;
import falgout.backup.app.Manager;

public class App implements FileStoreListener {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        Map<String, String> props = new LinkedHashMap<>();
        MessageDigest md;
        String algo = props.get("md");
        if (algo == null) {
            algo = "md5";
        }
        try {
            md = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not find message digest algorithm " + algo + ". Reverting to md5.");
            md = MessageDigest.getInstance("md5");
        }
        
        Injector i = Guice.createInjector(new BackupModule(props), new DeviceModule(md));
        i.getInstance(App.class).start();
    }
    
    private final FileStorePoller poll = new FileStorePoller();
    private final Manager manager;
    private final DeviceFactory factory;
    
    @Inject
    public App(Manager manager, DeviceFactory factory) {
        this.manager = manager;
        this.factory = factory;
    }
    
    public void start() {
        poll.addListener(this);
        poll.start();
    }
    
    @Override
    public void fileStoreAdded(final FileStore store) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!manager.isConfigured(store)) {
                        if (notifyUser(store)) {
                            configure(store);
                        }
                    } else {
                        backup(store);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override
    public void fileStoreRemoved(FileStore store) {}
    
    private boolean notifyUser(FileStore store) {
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Would you like to configure " + store
                + "?", "New device detected!", JOptionPane.YES_NO_OPTION);
    }
    
    private void configure(FileStore store) throws IOException {
        Device d = factory.create(store);
        
    }
    
    private void backup(final FileStore store) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    manager.backup(store);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
