package middleware.app;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
	
	public static void main(String argv[]) {
		// Folder we are going to watch
		Path folder = Paths.get(System.getProperty("user.dir") + "\\orders");
		WatchAndParse wp = new WatchAndParse(5);
		wp.watchDirectory(folder);
	}
}
