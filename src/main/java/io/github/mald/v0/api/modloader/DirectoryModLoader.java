package io.github.mald.v0.api.modloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.mald.v0.api.plugin.LoaderPlugin;

public abstract class DirectoryModLoader<T extends ModMetadata> extends AbstractModLoader<T> {
	final File directory;

	public DirectoryModLoader(LoaderPlugin plugin, File file) {
		super(plugin);
		this.directory = file;
	}

	@Override
	protected List<ModFiles> resolveModFiles() throws IOException {
		File[] files = this.directory.listFiles();
		List<ModFiles> paths = new ArrayList<>();
		for(File file : files) {
			paths.add(ModFiles.autoDetect(file.toPath()));
		}
		return paths;
	}
}
