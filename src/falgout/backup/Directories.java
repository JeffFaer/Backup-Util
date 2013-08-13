package falgout.backup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class Directories {
    private static class FileActionInvocationHandler<T> implements InvocationHandler {
        private final FileVisitor<? super T> action;
        private final FileVisitor<? super T> monitor;
        
        public FileActionInvocationHandler(FileVisitor<? super T> action, FileVisitor<? super T> monitor) {
            this.action = action;
            this.monitor = monitor;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(FileVisitor.class)) {
                FileVisitResult r;
                try {
                    r = (FileVisitResult) method.invoke(monitor, args);
                    if (r != FileVisitResult.TERMINATE) {
                        FileVisitResult r2 = (FileVisitResult) method.invoke(action, args);
                        if (r2 == FileVisitResult.TERMINATE) { return r2; }
                    }
                    return r;
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            } else {
                return method.invoke(proxy, args);
            }
        }
    }
    
    public static class Delete<P extends Path> extends SimpleFileVisitor<P> {
        @Override
        public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
            Files.deleteIfExists(file);
            return super.visitFile(file, attrs);
        }
        
        @Override
        public FileVisitResult postVisitDirectory(P dir, IOException exc) throws IOException {
            Files.deleteIfExists(dir);
            return super.postVisitDirectory(dir, exc);
        }
    }
    
    public static abstract class Copy<P extends Path> extends SimpleFileVisitor<P> {
        private final Path from;
        private final Path to;
        private final CopyOption[] options;
        
        public Copy(Path from, Path to, CopyOption... options) {
            this.from = from;
            this.to = to;
            this.options = options;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(P dir, BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(to.resolve(from.relativize(dir)), getAttributes(dir, attrs));
            return super.preVisitDirectory(dir, attrs);
        }
        
        protected abstract FileAttribute<?>[] getAttributes(P dir, BasicFileAttributes attrs);
        
        @Override
        public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, to.resolve(from.relativize(file)), options);
            return super.visitFile(file, attrs);
        }
    }
    
    public static class DefaultCopy<P extends Path> extends Copy<P> {
        public DefaultCopy(Path from, Path to, CopyOption... options) {
            super(from, to, options);
        }
        
        @Override
        protected FileAttribute<?>[] getAttributes(Path dir, BasicFileAttributes attrs) {
            return new FileAttribute<?>[0];
        }
    }
    
    public static class Enumerator<P extends Path> extends SimpleFileVisitor<P> {
        private final List<P> paths = new ArrayList<>();
        
        @Override
        public FileVisitResult preVisitDirectory(P dir, BasicFileAttributes attrs) throws IOException {
            paths.add(dir);
            return super.preVisitDirectory(dir, attrs);
        }
        
        @Override
        public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
            paths.add(file);
            return super.visitFile(file, attrs);
        }
        
        public List<P> getPaths() {
            return paths;
        }
    }
    
    public static class Digester<P extends Path> extends SimpleFileVisitor<P> {
        private final MessageDigest md;
        
        public Digester(MessageDigest md) {
            this.md = md;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(P dir, BasicFileAttributes attrs) throws IOException {
            md.update(dir.toString().getBytes());
            return super.preVisitDirectory(dir, attrs);
        }
        
        @Override
        public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
            md.update(file.toString().getBytes());
            update(file, md);
            return super.visitFile(file, attrs);
        }
    }
    
    public static class StructureChecker<P extends Path> extends SimpleFileVisitor<P> {
        private final FileChecker<? super Path> checker;
        private final P d1;
        private final P d2;
        
        private boolean lastCheck = true;
        
        public StructureChecker(FileChecker<? super Path> checker, P d1, P d2) {
            this.checker = checker;
            this.d1 = d1;
            this.d2 = d2;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(P dir, BasicFileAttributes attrs) throws IOException {
            return checkFile(dir) ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }
        
        @Override
        public FileVisitResult visitFile(P file, BasicFileAttributes attrs) throws IOException {
            return checkFile(file) ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }
        
        private boolean checkFile(P file) {
            return lastCheck = checker.areFilesSame(file, d2.resolve(d1.relativize(file)));
        }
        
        public boolean shouldContinue() {
            return lastCheck;
        }
    }
    
    public static interface FileChecker<P extends Path> {
        public boolean areFilesSame(P p1, P p2);
    }
    
    public static class FileExistsChecker<P extends Path> implements FileChecker<P> {
        @Override
        public boolean areFilesSame(P p1, P p2) {
            return (Files.exists(p1) && Files.exists(p2)) || (Files.notExists(p1) && Files.exists(p2));
        }
    }
    
    public static final FileVisitor<Object> NO_MONITOR = new SimpleFileVisitor<Object>() {};
    public static final EnumSet<FileVisitOption> NO_OPTIONS = EnumSet.noneOf(FileVisitOption.class);
    
    private Directories() {}
    
    public static <T> FileVisitor<T> createMonitoredFileVisitor(FileVisitor<? super T> action,
            FileVisitor<? super T> monitor) {
        return (FileVisitor<T>) Proxy.newProxyInstance(Directories.class.getClassLoader(),
                new Class<?>[] { FileVisitor.class }, new FileActionInvocationHandler<>(action, monitor));
    }
    
    public static void delete(Path dir) throws IOException {
        delete(dir, NO_MONITOR);
    }
    
    public static void delete(Path dir, FileVisitor<? super Path> monitor) throws IOException {
        delete(dir, monitor, NO_OPTIONS);
    }
    
    public static void delete(Path dir, FileVisitor<? super Path> monitor, Set<FileVisitOption> options)
            throws IOException {
        if (Files.notExists(dir)) { return; }
        
        Files.walkFileTree(dir, options, Integer.MAX_VALUE, createMonitoredFileVisitor(new Delete<>(), monitor));
    }
    
    public static void copy(Path from, Path to, CopyOption... options) throws IOException {
        copy(from, to, NO_MONITOR, options);
    }
    
    public static void copy(Path from, Path to, FileVisitor<? super Path> monitor, CopyOption... options)
            throws IOException {
        copy(from, to, monitor, NO_OPTIONS, Integer.MAX_VALUE, options);
    }
    
    public static void copy(Path from, Path to, FileVisitor<? super Path> monitor, Set<FileVisitOption> visitOptions,
            int maxDepth, CopyOption... options) throws IOException {
        Files.walkFileTree(from, visitOptions, maxDepth,
                createMonitoredFileVisitor(new DefaultCopy<>(from, to, options), monitor));
    }
    
    public static List<Path> enumerateEntries(Path dir) throws IOException {
        return enumerateEntries(dir, NO_OPTIONS, Integer.MAX_VALUE);
    }
    
    public static List<Path> enumerateEntries(Path dir, Set<FileVisitOption> options, int maxDepth) throws IOException {
        Enumerator<Path> e = new Enumerator<>();
        Files.walkFileTree(dir, options, maxDepth, e);
        return e.getPaths();
    }
    
    public static boolean isStructureSame(Path d1, Path d2) throws IOException {
        return isStructureSame(d1, d2, NO_MONITOR);
    }
    
    public static boolean isStructureSame(Path d1, Path d2, FileVisitor<? super Path> monitor) throws IOException {
        return isStructureSame(d1, d2, monitor, new FileExistsChecker<>());
    }
    
    public static boolean isStructureSame(Path d1, Path d2, FileVisitor<? super Path> monitor,
            FileChecker<? super Path> checker) throws IOException {
        return isStructureSame(d1, d2, monitor, checker, NO_OPTIONS, Integer.MAX_VALUE);
    }
    
    public static boolean isStructureSame(Path d1, Path d2, FileVisitor<? super Path> monitor,
            FileChecker<? super Path> checker, Set<FileVisitOption> options, int maxDepth) throws IOException {
        return isStructureSame(d1, d2, monitor, checker, options, maxDepth, true);
    }
    
    private static boolean isStructureSame(Path d1, Path d2, FileVisitor<? super Path> monitor,
            FileChecker<? super Path> checker, Set<FileVisitOption> options, int maxDepth, boolean swapDirectories)
            throws IOException {
        StructureChecker<Path> sc = new StructureChecker<>(checker, d1, d2);
        Files.walkFileTree(d1, options, maxDepth, createMonitoredFileVisitor(sc, monitor));
        
        if (sc.shouldContinue()) {
            return swapDirectories ? isStructureSame(d2, d1, monitor, checker, options, maxDepth, false) : true;
        } else {
            return false;
        }
    }
    
    public static byte[] digest(Path dir, MessageDigest md) throws IOException {
        return digest(dir, md, NO_MONITOR);
    }
    
    public static byte[] digest(Path dir, MessageDigest md, FileVisitor<? super Path> monitor) throws IOException {
        return digest(dir, md, monitor, NO_OPTIONS, Integer.MAX_VALUE);
    }
    
    public static byte[] digest(Path dir, MessageDigest md, FileVisitor<? super Path> monitor,
            Set<FileVisitOption> options, int maxDepth) throws IOException {
        if (Files.isDirectory(dir)) {
            Files.walkFileTree(dir, options, maxDepth, createMonitoredFileVisitor(new Digester<>(md), monitor));
        } else {
            update(dir, md);
        }
        return md.digest();
    }
    
    private static void update(Path file, MessageDigest md) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = in.read(buf)) > 0) {
                md.update(buf, 0, read);
            }
        }
    }
}
