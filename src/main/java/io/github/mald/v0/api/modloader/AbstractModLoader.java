package io.github.mald.v0.api.modloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.mald.impl.LoaderPluginLoader;
import io.github.mald.impl.mixin.MaldMixinBootstrap;
import io.github.mald.v0.api.classloader.MainClassLoader;

public abstract class AbstractModLoader<T extends ModMetadata> implements AutoCloseable, ModLoader<T> {
	List<Path> modFiles;
	List<FileSystem> systems;
	List<T> mods;

	public final List<Path> getModFiles() {
		List<Path> paths = this.modFiles;
		if(paths == null) {
			this.modFiles = paths = this.resolveMods();
			this.systems = new ArrayList<>(paths.size());
			for(Path mod : this.modFiles) {
				try {
					this.systems.add(FileSystems.newFileSystem(mod, (ClassLoader) null));
				} catch(IOException e) {
					throw LoaderPluginLoader.rethrow(e);
				}
			}
		}
		return paths;
	}

	@Override
	public List<T> getMods() {
		List<T> mods = this.mods;
		if(mods == null) {
			try {
				this.initializeMods();
			} catch(IOException e) {
				throw LoaderPluginLoader.rethrow(e);
			}
			mods = this.mods;
		}
		return mods;
	}

	@Override
	public void close() throws Exception {
		if(this.systems != null) {
			for(FileSystem system : this.systems) {
				system.close();
			}
		}
	}

	@Override
	public void init(MainClassLoader loader) throws MalformedURLException {
		for(Path mod : this.getModFiles()) {
			loader.offer(mod.toUri().toURL());
		}
	}

	protected abstract List<Path> resolveMods();

	protected abstract T extractMetadata(Path path, FileSystem system) throws IOException;

	protected final void initializeMods() throws IOException {
		this.getModFiles();
		this.mods = new ArrayList<>(this.systems.size());
		for(int i = 0; i < this.systems.size(); i++) {
			FileSystem system = this.systems.get(i);
			Path path = this.modFiles.get(i);
			T meta = this.extractMetadata(path, system);
			if(meta != null) {
				this.mods.add(meta);
			}
		}

		// Why Java? I can tell that this cast is fine. why do I even need to cast - hydos
		MaldMixinBootstrap.loadMixinMods((List<ModMetadata>) this.mods);
	}
}
