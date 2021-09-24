package io.github.mald.v0.api.modloader;

import java.io.File;
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
	protected List<Path> resolveMods() {
		File[] files = this.directory.listFiles();
		List<Path> paths = new ArrayList<>();
		for(File file : files) {
			paths.add(file.toPath());
		}
		return paths;
	}

}
