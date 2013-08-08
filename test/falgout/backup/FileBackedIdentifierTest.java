package falgout.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.UUID;

import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
public class FileBackedIdentifierTest {
    @Rule @Inject public TemporaryFileStore fs;
    private FileBackedIdentifier i;
    
    @Before
    public void init() throws IOException {
        i = new FileBackedIdentifier(fs.locator, FileBackedIdentifier.DEFAULT_ID_FILE);
    }
    
    @Test
    public void NullIfNotSet() throws IOException {
        assertNull(i.getID(fs.store));
    }
    
    @Test
    public void ConsistentIfSet() throws IOException {
        UUID id = UUID.randomUUID();
        i.setID(fs.store, id);
        assertEquals(id, i.getID(fs.store));
    }
    
    @Test
    public void CannotChangeID() throws IOException {
        UUID id = UUID.randomUUID();
        i.setID(fs.store, id);
        i.setID(fs.store, UUID.randomUUID());
        assertEquals(id, i.getID(fs.store));
    }
}
