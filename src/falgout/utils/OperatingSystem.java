package falgout.utils;

import java.util.regex.Pattern;

public final class OperatingSystem {
	private OperatingSystem() {
	}
	
	public static final String OS_NAME = "os.name";
	
	private static String OS_PROPERTY;
	
	private static boolean WINDOWS;
	private static boolean MAC;
	private static boolean LINUX;
	private static boolean DEC;
	private static boolean UNIX;
	
	static {
		determineOS();
	}
	
	private static void determineOS() {
		OS_PROPERTY = System.getProperty(OS_NAME);
		
		if (osMatches("Windows")) {
			WINDOWS = true;
		} else if (osMatches("Mac")) {
			MAC = true;
		} else if (osMatches("Linux")) {
			LINUX = true;
		} else if (osMatches("OpenVMS")) {
			DEC = true;
		} else {
			UNIX = true;
		}
	}
	
	private static boolean osMatches(String os) {
		Pattern p = Pattern.compile(os, Pattern.CASE_INSENSITIVE);
		return p.matcher(OS_PROPERTY).find();
	}
	
	public static boolean isWindows() {
		return WINDOWS;
	}
	
	public static boolean isMac() {
		return MAC;
	}
	
	public static boolean isLinux() {
		return LINUX;
	}
	
	public static boolean isDecOs() {
		return DEC;
	}
	
	public static boolean isUnix() {
		return UNIX;
	}
}
