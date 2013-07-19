package falgout.utils;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class OperatingSystemTest {
	private static final Method determine;
	static {
		try {
			determine = OperatingSystem.class.getDeclaredMethod("determineOS");
			determine.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
	}
	
	private void set(String os) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		System.setProperty("os.name", os);
		determine.invoke(null);
	}
	
	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		set("Windows 7");
		assertTrue(OperatingSystem.isWindows());
	}
	
	@Test
	public void MacTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		set("Mac OS");
		assertTrue(OperatingSystem.isMac());
	}
	
	@Test
	public void LinuxTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		set("Linux");
		assertTrue(OperatingSystem.isLinux());
	}
	
	@Test
	public void DecOsTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		set("OpenVMS");
		assertTrue(OperatingSystem.isDecOs());
	}
	
	@Test
	public void UnixTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		set("HP-UX");
		assertTrue(OperatingSystem.isUnix());
	}
}
