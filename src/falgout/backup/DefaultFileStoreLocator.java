package falgout.backup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

enum DefaultFileStoreLocator implements FileStoreLocator {
	LINUX {
		private final Path mounts = Paths.get("/proc/mounts");
		private final boolean exists = mounts.isAbsolute() && Files.exists(mounts);
		
		@Override
		public Path getRootLocation(FileStore store) throws IOException {
			if (exists) {
				List<String> lines = Files.readAllLines(mounts, Charset.defaultCharset());
				
				// /proc/mounts is listed in mount order. Only the most recent
				// mount at a location will be visible.
				Collections.reverse(lines);
				
				for (String line : lines) {
					String[] parts = line.split("\\s");
					Path location = Paths.get(parts[1].replace("\\040", " "));
					FileStore fs = Files.getFileStore(location);
					
					if (fs.equals(store)) { return location; }
				}
			}
			
			return null;
		}
	},
	MAC_OS {
		@Override
		public Path getRootLocation(FileStore store) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	WINDOWS {
		@Override
		public Path getRootLocation(FileStore store) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	@Override
	public abstract Path getRootLocation(FileStore store) throws IOException;
}
