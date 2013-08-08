package falgout.backup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.FileStore;

public class TemporaryFileStore extends TemporaryFileStructure {
    public final FileStore store = mock(FileStore.class);
    public final FileStoreLocator locator = mock(FileStoreLocator.class);
    
    @Override
    protected void before() throws Throwable {
        super.before();
        when(locator.getRootLocation(store)).thenReturn(dir);
    }
}
