package falgout.backup.app;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import falgout.backup.Directories;
import falgout.backup.Directory;

public class HistoryTest {
	private static final long msb = 12345;
	private static final long lsb = 67890;
	private static final UUID id = new UUID(msb, lsb);
	
	private History h;
	
	@Before
	public void init() throws IOException {
		h = History.get(id);
	}
	
	@After
	public void after() throws IOException {
		if (Files.exists(History.HISTORY_DIR)) {
			Directories.delete(Directory.get(History.HISTORY_DIR));
		}
	}
	
	@Test
	public void HistoryInstancesAreSame() throws IOException {
		History h2 = History.get(id);
		assertSame(h, h2);
	}
	
	@Test
	public void SavesAutomaticallyOnAliasChange() throws IOException {
		h.addAlias(Paths.get("."));
		
		checkFile();
	}
	
	private void checkFile() {
		assertTrue(Files.exists(History.HISTORY_DIR.resolve(h.getID().toString())));
	}
	
	@Test
	public void SavesAutomaticallyOnHashChange() throws IOException {
		h.updateHash(Paths.get("."), new Hash(new byte[0]));
		
		checkFile();
	}
	
	@Test
	public void ManuallyLoadingHistoryFilePreservesIdentity() throws IOException, ClassNotFoundException {
		h.addAlias(Paths.get("bin"));
		assertSame(h, load(h.getID()));
	}
	
	private History load(UUID id) throws ClassNotFoundException, IOException {
		try (InputStream in = Files.newInputStream(History.HISTORY_DIR.resolve(id.toString()));
				ObjectInputStream ois = new ObjectInputStream(in);) {
			return (History) ois.readObject();
		}
	}
	
	@Test
	public void ManuallyLoadingMultipleTimesPreservesIdentity() throws IOException, IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
		History h2 = History.get(UUID.randomUUID());
		h2.addAlias(Paths.get("foo"));
		
		Field f = History.class.getDeclaredField("CACHE");
		f.setAccessible(true);
		Map<?, ?> cache = (Map<?, ?>) f.get(null);
		cache.remove(h2.getID());
		
		History h3 = load(h2.getID());
		History h4 = load(h2.getID());
		
		assertNotSame(h2, h3);
		assertSame(h3, h4);
	}
}
