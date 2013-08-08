package falgout.backup;

import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.jukito.JukitoRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
public class TemporaryFileStoreTest {
    @Rule @Inject public TemporaryFileStore fs;
    
    @Test
    public void LocatorReturnsFileStructureRoot() throws IOException {
        assertSame(fs.dir, fs.locator.getRootLocation(fs.store));
    }
}
