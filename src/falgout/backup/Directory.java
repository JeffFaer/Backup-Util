package falgout.backup;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import falgout.utils.AbstractIterator;

public class Directory implements Comparable<Directory>, Iterable<Path> {
	private static class OrDirectory implements Filter<Path> {
		private final Filter<? super Path> f;
		
		public OrDirectory(Filter<? super Path> f) {
			this.f = f;
		}
		
		@Override
		public boolean accept(Path entry) throws IOException {
			return f.accept(entry) || Files.isDirectory(entry);
		}
	}
	
	private class DirectoryIterator extends AbstractIterator<Path> {
		private final Filter<? super Path> finalFilter;
		private final Filter<Path> streamFilter;
		
		private final Queue<DirectoryStream<Path>> streams = Collections.asLifoQueue(new LinkedList<DirectoryStream<Path>>());
		private final Queue<Iterator<Path>> itrs = Collections.asLifoQueue(new LinkedList<Iterator<Path>>());
		
		public DirectoryIterator(Filter<? super Path> finalFilter) {
			this.finalFilter = finalFilter;
			streamFilter = new OrDirectory(finalFilter);
			
			createStream(dir);
		}
		
		private void createStream(Path dir) {
			try {
				DirectoryStream<Path> stream = Files.newDirectoryStream(dir, streamFilter);
				Iterator<Path> itr = stream.iterator();
				
				streams.add(stream);
				itrs.add(itr);
			} catch (IOException e) {
				throw new DirectoryIteratorException(e);
			}
		}
		
		@Override
		protected Path findNext() {
			try {
				Iterator<Path> itr;
				while ((itr = itrs.peek()) != null && !itr.hasNext()) {
					close();
				}
				
				if (itr == null) {
					return null;
				}
				
				while (itr.hasNext()) {
					Path f = itr.next();
					
					boolean ret = finalFilter.accept(f);
					
					if (Files.isDirectory(f)) {
						createStream(f);
						if (!ret) {
							// start iterating over nested directory if
							// we aren't going to return the directory
							return findNext();
						}
					}
					if (ret) {
						return dir.relativize(f);
					}
				}
				
				return null;
			} catch (IOException e) {
				throw new DirectoryIteratorException(e);
			}
		}
		
		private void close() throws IOException {
			streams.poll().close();
			itrs.poll();
		}
	}
	
	private static final Filter<Object> ACCEPT_ALL = new Filter<Object>() {
		@Override
		public boolean accept(Object entry) throws IOException {
			return true;
		}
	};
	
	private final Path dir;
	
	private Directory(Path dir) {
		this.dir = dir;
	}
	
	public Path getPath() {
		return dir;
	}
	
	public Path resolve(String other) {
		return dir.resolve(other);
	}
	
	public Path resolve(Path other) {
		return dir.resolve(other);
	}
	
	public Path relativize(Path other) {
		return dir.relativize(other);
	}
	
	@Override
	public int compareTo(Directory o) {
		return dir.compareTo(o.dir);
	}
	
	@Override
	public Iterator<Path> iterator() {
		return iterator(ACCEPT_ALL);
	}
	
	public Iterator<Path> iterator(Filter<? super Path> p) {
		return new DirectoryIterator(p);
	}
	
	public Iterable<Path> iterable(final Filter<? super Path> p) {
		return new Iterable<Path>() {
			@Override
			public Iterator<Path> iterator() {
				return Directory.this.iterator(p);
			}
		};
	}
	
	public Iterator<Path> iterator(final Syntax syntax, final String pattern) {
		final PathMatcher m = FileSystems.getDefault().getPathMatcher(syntax + ":" + pattern);
		return iterator(new Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return m.matches(entry);
			}
		});
	}
	
	public Iterable<Path> iterable(final Syntax syntax, final String pattern) {
		return new Iterable<Path>() {
			@Override
			public Iterator<Path> iterator() {
				return Directory.this.iterator(syntax, pattern);
			}
		};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dir == null) ? 0 : dir.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Directory)) {
			return false;
		}
		Directory other = (Directory) obj;
		if (dir == null) {
			if (other.dir != null) {
				return false;
			}
		} else if (!dir.equals(other.dir)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Directory [dir=");
		builder.append(dir);
		builder.append("]");
		return builder.toString();
	}
	
	public static Directory get(String dir) throws IOException {
		return get(Paths.get(dir));
	}
	
	/**
	 * 
	 * @param dir A {@code Path} pointing to an existing directory.
	 * @return A {@code Directory} of the given {@code Path}
	 * @throws IOException If {@code dir} does not exist.
	 */
	public static Directory get(Path dir) throws IOException {
		if (Files.notExists(dir)) {
			throw new NoSuchFileException(dir.toString());
		} else if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException(dir + " is not a directory.");
		}
		return new Directory(dir);
	}
	
	public static Directory create(String dir) throws IOException {
		return create(Paths.get(dir));
	}
	
	public static Directory create(Path dir) throws IOException {
		Files.createDirectories(dir);
		return new Directory(dir);
	}
}
