package worldcleaner.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	public static void init() {
	}

	public static String getAbsoluteFileName(String path) {
		return new File(path).getAbsolutePath();
	}

	public static List<String> splitPath(String path) {
		List<String> paths = new ArrayList<>();
		for (String split : path.split("[/]")) {
			if (!split.isEmpty()) {
				paths.add(split);
			}
		}
		return paths;
	}

	public static File buildFile(File file, String... childs) {
		File result = file;
		for (String child : childs) {
			result = new File(result, child);
		}
		return result;
	}

	public static void deleteDirectory(File file) throws IOException {
		if (!file.exists()) {
			return;
		}
		Files.walkFileTree(file.toPath(), new FileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		file.delete();
	}

	public static File[] safeListFiles(File file) {
		File[] files = file.listFiles();
		return files != null ? files : new File[0];
	}

	public static String[] safeList(File file) {
		String[] files = file.list();
		return files != null ? files : new String[0];
	}

}
