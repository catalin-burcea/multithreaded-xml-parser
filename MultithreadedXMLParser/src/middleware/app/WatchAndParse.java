package middleware.app;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WatchAndParse {

	private ExecutorService executor;
	
	public WatchAndParse(int poolSize) {
		this.executor = Executors.newFixedThreadPool(poolSize);
	}

	private boolean isValidFileName(String fileName) {
		if (!fileName.substring(0, 6).equals("orders") || !fileName.substring(fileName.length() - 4).equals(".xml")) {
			return false;
		}
		fileName = fileName.substring(6);
		fileName = fileName.substring(0, fileName.length() - 4);

		for (int i = 0; i < fileName.length(); i++) {
			if (!Character.isDigit(fileName.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private void processCreateFileEvent(WatchEvent<?> watchEvent, Path path) {

		@SuppressWarnings("unchecked")
		Path newPath = ((WatchEvent<Path>) watchEvent).context();
		File newOrdersFile = new File(path + "\\" + newPath.toFile().getName());
		if (newOrdersFile.isFile() && this.isValidFileName(newOrdersFile.getName())) {
			Runnable worker = new WorkerThread(newOrdersFile);
			this.executor.execute(worker);
		}
	}

	public void watchDirectory(Path path) {
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path + " is not a folder");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("Watching path: " + path);
		FileSystem fs = path.getFileSystem();
		try (WatchService service = fs.newWatchService()) {
			path.register(service, ENTRY_CREATE);
			while (true) {

				WatchKey key = service.poll(50,TimeUnit.MILLISECONDS);
				if (key != null) {
					for (WatchEvent<?> watchEvent : key.pollEvents()) {

						Kind<?> kind = watchEvent.kind();
						if (OVERFLOW == kind) {
							continue;
						} else if (ENTRY_CREATE == kind) {
							this.processCreateFileEvent(watchEvent, path);
						}
					}

					if (!key.reset()) {
						break;
					}
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}
