package falgout.backup;

import java.io.IOException;
import java.nio.file.FileStore;
import java.util.UUID;

import com.google.inject.ImplementedBy;

@ImplementedBy(FileBackedIdentifier.class)
public interface FileStoreIdentifier {
    /**
     * Finds a {@code UUID} associated with a {@code FileStore}.
     * 
     * @param store The {@code FileStore}.
     * @return The {@code UUID}, or {@code null} if one does not yet exist.
     * @throws IOException If an I/O exception occurs.
     */
    public UUID getID(FileStore store) throws IOException;
    
    /**
     * Attempts to set the {@code UUID} associated with the {@code FileStore},
     * if possible. If it is not possible (because one is already set), then the
     * previously set {@code UUID} is returned.
     * 
     * @param store The {@code FileStore}.
     * @param id The {@code UUID} to associate with the {@code FileStore}.
     * @return The {@code UUID} that is now associated with the
     *         {@code FileStore}.
     * @throws IOException If an I/O exception occurs.
     */
    public UUID setID(FileStore store, UUID id) throws IOException;
}
