package falgout.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Directories {
	public static final FileVisitor<Path> DO_NOTHING = new SimpleFileVisitor<Path>() {
	};
	
	private Directories() {
	}
	
	public static void delete(Directory dir) throws IOException {
		delete(dir, DO_NOTHING);
	}
	
	public static void delete(Directory dir, final FileVisitor<? super Path> progressMonitor) throws IOException {
		if (Files.notExists(dir.getPath())) {
			return;
		}
		
		Files.walkFileTree(dir.getPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				progressMonitor.preVisitDirectory(dir, attrs);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				
				progressMonitor.visitFile(file, attrs);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				progressMonitor.visitFileFailed(file, exc);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				
				progressMonitor.postVisitDirectory(dir, exc);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public static void copy(Directory source, Directory target) throws IOException {
		copy(source, target, DO_NOTHING);
	}
	
	public static void copy(final Directory source, final Directory target,
			final FileVisitor<? super Path> progressMonitor) throws IOException {
		Files.walkFileTree(source.getPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir)));
				
				progressMonitor.preVisitDirectory(dir, attrs);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)));
				
				progressMonitor.visitFile(file, attrs);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				progressMonitor.visitFileFailed(file, exc);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				progressMonitor.postVisitDirectory(dir, exc);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public static boolean isStructureSame(Directory d1, Directory d2) throws IOException {
		return isStructureSame(d1, d2, DO_NOTHING);
	}
	
	public static boolean isStructureSame(final Directory d1, final Directory d2,
			final FileVisitor<? super Path> progressMonitor) throws IOException {
		final AtomicBoolean result = new AtomicBoolean(true);
		
		Files.walkFileTree(d1.getPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				progressMonitor.preVisitDirectory(dir, attrs);
				return check(dir);
			}
			
			private FileVisitResult check(Path f) {
				if (Files.exists(d2.resolve(d1.relativize(f)))) {
					return FileVisitResult.CONTINUE;
				} else {
					result.set(false);
					return FileVisitResult.TERMINATE;
				}
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				progressMonitor.visitFile(file, attrs);
				return check(file);
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				progressMonitor.visitFileFailed(file, exc);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				progressMonitor.postVisitDirectory(dir, exc);
				return FileVisitResult.CONTINUE;
			}
		});
		
		return result.get();
	}
	
	public static byte[] digest(Directory dir, MessageDigest md) throws IOException {
		Set<Path> paths = new TreeSet<>();
		for (Path p : dir) {
			paths.add(dir.resolve(p));
		}
		
		List<InputStream> streams = new ArrayList<>(paths.size());
		for (Path p : paths) {
			if (Files.isRegularFile(p)) {
				streams.add(Files.newInputStream(p));
			}
			md.update(p.toString().getBytes());
		}
		
		try (InputStream is = new SequenceInputStream(Collections.enumeration(streams))) {
			byte[] buf = new byte[1024];
			int read;
			while ((read = is.read(buf)) > 0) {
				md.update(buf, 0, read);
			}
		}
		
		return md.digest();
	}
}
