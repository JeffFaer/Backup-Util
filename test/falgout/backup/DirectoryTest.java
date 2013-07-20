package falgout.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryTest {
	private static Path tmp;
	private static Path file1;
	private static Path file2;
	private static Path file3;
	private static Path nonExistant;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		tmp = Files.createTempDirectory("test");
		Files.createDirectory(tmp.resolve("foo"));
		Files.createFile(file1 = tmp.resolve("file1"));
		Files.createFile(file2 = tmp.resolve("file2"));
		Files.createFile(file3 = tmp.resolve("foo/file3"));
		
		nonExistant = tmp.resolve("IDontExist");
	}
	
	@AfterClass
	public static void afterClass() throws IOException {
		Files.walkFileTree(tmp, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	@Test(expected = IOException.class)
	public void GettingDirectoryMustExist() throws IOException {
		Directory.get(nonExistant);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void GettingDirectoryMustBeDirectory() throws IOException {
		Directory.get(file1);
	}
	
	@Test(expected = IOException.class)
	public void CreatingDirectoryMustNotBeFile() throws IOException {
		Directory.create(file1);
	}
	
	@Test
	public void CreatingDirectoryMakesNew() throws IOException {
		assertTrue(Files.notExists(nonExistant));
		Directory.create(nonExistant);
		assertTrue(Files.exists(nonExistant));
		Files.delete(nonExistant);
	}
	
	@Test
	public void IteratingIsRecursive() throws IOException {
		Directory d = Directory.get(tmp);
		Set<Path> files = new LinkedHashSet<>();
		for (Path file : d.iterable(new Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isRegularFile(entry);
			}
		})) {
			files.add(file);
		}
		
		assertEquals(
				new LinkedHashSet<>(Arrays.asList(tmp.relativize(file1), tmp.relativize(file2), tmp.relativize(file3))),
				files);
	}
	
	@Test
	public void IteratingOverRelativeDirectoryWorks() throws IOException {
		Directory d = Directory.get(".");
		Set<Path> files = new LinkedHashSet<>();
		for (Path p : d) {
			files.add(p);
		}
		
		assertTrue(files.contains(Paths.get("LICENSE")));
		assertTrue(files.contains(Paths.get("README.md")));
		assertTrue(files.contains(Paths.get("src/falgout/backup/Directory.java")));
	}
}
