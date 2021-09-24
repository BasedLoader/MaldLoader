package io.github.mald.v0.api.modloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.mald.v0.api.classloader.MainClassLoader;
import org.jetbrains.annotations.Nullable;

public class ModFiles implements AutoCloseable {
	public static final ModFiles EMPTY = new ModFiles(Collections.emptyList(), Collections.emptyList());
	public final Iterable<Path> files;
	public final Iterable<Path> roots;
	private final AutoCloseable onClose;

	public static ModFiles autoDetect(Path path) throws IOException {
		if(!Files.exists(path)) {
			return EMPTY;
		} else if(Files.isDirectory(path)) {
			return directory(path);
		} else {
			return jar(path);
		}
	}

	public static ModFiles directory(Iterable<Path> directories) {
		return new ModFiles(directories, directories);
	}

	public static ModFiles directory(Path... directory) {
		List<Path> paths = Arrays.asList(directory);
		return new ModFiles(paths, paths);
	}

	/**
	 * jar or zip or whatever
	 */
	public static ModFiles jar(Path path) throws IOException {
		FileSystem system = FileSystems.newFileSystem(path, (ClassLoader) null);
		return new ModFiles(path, system.getRootDirectories(), system);
	}

	public ModFiles(Path path, Iterable<Path> roots) {
		this(Collections.singletonList(path), roots, null);
	}

	public ModFiles(Path path, Path roots, AutoCloseable close) {
		this(Collections.singletonList(path), roots, close);
	}

	public ModFiles(Path path, Iterable<Path> roots, AutoCloseable close) {
		this(Collections.singletonList(path), roots, close);
	}

	public ModFiles(Iterable<Path> files, Iterable<Path> roots) {
		this(files, roots, null);
	}

	public ModFiles(Iterable<Path> files, Iterable<Path> roots, AutoCloseable close) {
		this.files = files;
		this.roots = roots;
		this.onClose = close;
	}

	public ModFiles(Iterable<Path> files, Path root, AutoCloseable close) {
		this.files = files;
		this.roots = Collections.singletonList(root);
		this.onClose = close;
	}

	public void addTo(MainClassLoader loader) throws MalformedURLException {
		for(Path file : this.files) {
			loader.offer(file.toUri().toURL());
		}
	}

	@Override
	public void close() throws Exception {
		if(this.onClose != null) {
			this.onClose.close();
		}
	}

	@Nullable
	public Path resolveExists(String path) {
		for(Path file : this.roots) {
			Path resolved = file.resolve(path);
			if(Files.exists(resolved)) {
				return resolved;
			}
		}
		return null;
	}
}