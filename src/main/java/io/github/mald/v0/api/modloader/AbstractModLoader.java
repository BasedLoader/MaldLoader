package io.github.mald.v0.api.modloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.mald.impl.LoaderPluginLoader;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.mixin.MaldMixinBootstrap;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;

public abstract class AbstractModLoader<T extends ModMetadata> implements AutoCloseable, ModLoader<T> {
	final LoaderPlugin plugin;
	protected List<Path> modFiles;
	protected List<FileSystem> systems;
	protected Map<String, T> mods;

	protected AbstractModLoader(LoaderPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public LoaderPlugin originPlugin() {
		return this.plugin;
	}

	@Override
	public Map<String, T> getMods() {
		Map<String, T> mods = this.mods;
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
	public void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {
		for(Path mod : this.getModFiles()) {
			loader.offer(mod.toUri().toURL());
		}
	}

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
	public void close() throws Exception {
		if(this.systems != null) {
			for(FileSystem system : this.systems) {
				system.close();
			}
		}
	}

	protected abstract List<Path> resolveMods();

	protected abstract T extractMetadata(Path path, FileSystem system) throws IOException;

	protected void initializeMods() throws IOException {
		this.getModFiles();
		this.mods = new HashMap<>(this.systems.size());
		for(int i = 0; i < this.systems.size(); i++) { // must be for index to allow for adding while iterating for JiJ
			FileSystem system = this.systems.get(i);
			Path path = this.modFiles.get(i);
			T meta = this.extractMetadata(path, system);
			this.offerMod(meta);
		}
	}

	protected void offerMod(T meta) {
		if(meta != null) {
			T old = this.mods.get(meta.id());
			if(old != null) {
				this.mods.put(meta.id(), this.onModIdOverride(old, meta));
			} else {
				this.mods.put(meta.id(), meta);
			}
		}

		// Why Java? I can tell that this cast is fine. why do I even need to cast - hydos
		MaldMixinBootstrap.loadMixinMods((List<ModMetadata>) this.mods);
	}

	protected T onModIdOverride(T a, T b) {
		return a;
	}
}
