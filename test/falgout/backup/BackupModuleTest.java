package falgout.backup;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;

import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

import falgout.backup.guice.BackupLocation;
import falgout.backup.guice.ConfigurationDirectory;
import falgout.backup.guice.IdentificationFile;

@RunWith(JukitoRunner.class)
public class BackupModuleTest {
    @Inject private BackupModule m;
    
    @Test
    public void PathsAreBoundCorrectly() {
        Injector i = Guice.createInjector(m);
        assertEquals(BackupModule.DEFAULT_CONF_DIR, i.getInstance(Key.get(Path.class, ConfigurationDirectory.class)));
        assertEquals(BackupModule.DEFAULT_ID_FILE, i.getInstance(Key.get(Path.class, IdentificationFile.class)));
        assertEquals(BackupModule.DEFAULT_BACKUP_DIR, i.getInstance(Key.get(Path.class, BackupLocation.class)));
    }
}
