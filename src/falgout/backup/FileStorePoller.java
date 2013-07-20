package falgout.backup;

import java.io.Closeable;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import falgout.utils.CloseableLock;

/**
 * A {@code FileStorePoller} detects when a {@link FileStore} is added or
 * removed from a {@link FileSystem}. Once a {@code FileStorePoller} has been
 * {@link #start() started}, attempting to {@code start} it again without first
 * {@link #close() closing} it will result in an exception.<br/>
 * <br/>
 * This class is implemented via polling. Specifying a {@code delay} and
 * {@code TimeUnit} in the {@link #FileStorePoller(FileSystem, long, TimeUnit)
 * constructor} determines how quickly detection occurs.
 * 
 * @author jeffrey
 */
public class FileStorePoller implements Closeable {
	private final FileSystem fileSystem;
	private final long delay;
	private final TimeUnit unit;
	
	private final Lock lock = new ReentrantLock();
	private volatile ScheduledExecutorService executor;
	private final Set<FileStoreListener> listeners = new CopyOnWriteArraySet<>();
	
	public FileStorePoller() {
		this(FileSystems.getDefault());
	}
	
	public FileStorePoller(FileSystem fileSystem) {
		this(fileSystem, 1, TimeUnit.SECONDS);
	}
	
	public FileStorePoller(FileSystem fileSystem, long delay, TimeUnit unit) {
		this.fileSystem = fileSystem;
		this.delay = delay;
		this.unit = unit;
	}
	
	public FileSystem getFileSystem() {
		return fileSystem;
	}
	
	public long getDelay() {
		return delay;
	}
	
	public TimeUnit getTimeUnit() {
		return unit;
	}
	
	public boolean isRunning() {
		try (CloseableLock l = CloseableLock.lock(lock)) {
			return executor != null;
		}
	}
	
	/**
	 * Starts this {@code FileStorePoller} as a recurring task that repeats as
	 * specified when it was created.
	 * 
	 * @throws IllegalStateException If this {@code FileStorePoller} has already
	 *         been started.
	 */
	public void start() {
		try (CloseableLock l = CloseableLock.lock(lock)) {
			if (executor != null) {
				throw new IllegalStateException("Already started.");
			}
			
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleWithFixedDelay(new Runnable() {
				private List<FileStore> currentFileStores;
				
				@Override
				public void run() {
					if (currentFileStores == null) {
						currentFileStores = getFileStores();
						return;
					}
					
					try {
						List<FileStore> updatedFileStores = getFileStores();
						
						List<FileStore> newFileStores = new ArrayList<>();
						for (FileStore u : updatedFileStores) {
							if (!currentFileStores.contains(u)) {
								newFileStores.add(u);
							}
						}
						
						List<FileStore> removedFileStores = new ArrayList<>();
						for (FileStore o : currentFileStores) {
							if (!updatedFileStores.contains(o)) {
								removedFileStores.add(o);
							}
						}
						
						currentFileStores = updatedFileStores;
						
						for (FileStoreListener l : listeners) {
							for (FileStore n : newFileStores) {
								l.fileStoreAdded(n);
							}
							
							for (FileStore r : removedFileStores) {
								l.fileStoreRemoved(r);
							}
						}
					} catch (Throwable t) {
						t.printStackTrace();
						close();
					}
				}
			}, 0, delay, unit);
		}
	}
	
	private List<FileStore> getFileStores() {
		return asList(fileSystem.getFileStores());
	}
	
	private <T> List<T> asList(Iterable<T> i) {
		if (i instanceof List) {
			return (List<T>) i;
		}
		
		List<T> l = new ArrayList<>();
		for (T t : i) {
			l.add(t);
		}
		return l;
	}
	
	public void addListener(FileStoreListener l) {
		listeners.add(l);
	}
	
	public void removeListener(FileStoreListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Blocks until {@link #close} is called, the timeout elapses, or the
	 * current thread is interrupted.
	 * 
	 * @param timeout maximum time to wait
	 * @param unit unit of {@code timeout}
	 * @return {@code true} if the {@code FileStorePoller} terminated,
	 *         {@code false} if the timeout elapsed.
	 * @throws InterruptedException if interrupted while waiting
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}
	
	/**
	 * Stops this {@code FileStorePoller}.
	 * 
	 * @throws IllegalStateException If this {@code FileStorePoller} hasn't been
	 *         started yet.
	 */
	@Override
	public void close() {
		try (CloseableLock l = CloseableLock.lock(lock)) {
			if (executor == null) {
				throw new IllegalStateException("Not started.");
			}
			executor.shutdownNow();
			executor = null;
		}
	}
}
