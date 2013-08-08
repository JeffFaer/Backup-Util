package falgout.backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.backup.guice.IdentificationFile;

@Singleton
public class FileBackedIdentifier implements FileStoreIdentifier {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Path DEFAULT_ID_FILE = Paths.get(".dev-id");
    private final FileStoreLocator l;
    private final Path idFile;
    
    @Inject
    public FileBackedIdentifier(FileStoreLocator l, @IdentificationFile Path idFile) {
        this.l = l;
        this.idFile = idFile;
    }
    
    @Override
    public UUID getID(FileStore store) throws IOException {
        Path file = getIDFile(store);
        
        if (Files.exists(file)) {
            try (BufferedReader in = Files.newBufferedReader(file, CHARSET)) {
                return UUID.fromString(in.readLine());
            }
        }
        return null;
    }
    
    private Path getIDFile(FileStore store) throws IOException {
        return l.getRootLocation(store).resolve(idFile);
    }
    
    @Override
    public UUID setID(FileStore store, UUID id) throws IOException {
        Path file = getIDFile(store);
        
        if (Files.exists(file)) {
            id = getID(store);
        } else {
            try (BufferedWriter out = Files.newBufferedWriter(file, CHARSET, StandardOpenOption.CREATE_NEW)) {
                out.write(id.toString());
                out.write('\n');
            }
        }
        
        return id;
    }
}
