package io.github.mald.v0.api.modloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.mald.impl.classloader.Main;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractModLoader<T extends ModMetadata> implements AutoCloseable, ModLoader<T> {
	private static final Logger LOGGER = LogManager.getLogManager().getLogger("AbstractModLoader");
	final LoaderPlugin plugin;
	List<ModFiles> resolvedFiles;
	Map<String, T> mods;

	protected AbstractModLoader(LoaderPlugin plugin) {this.plugin = plugin;}

	public List<ModFiles> getFiles() {
		if(this.resolvedFiles == null) {
			try {
				return this.resolvedFiles = this.resolveModFiles();
			} catch(IOException e) {
				throw Main.rethrow(e);
			}
		} else {
			return this.resolvedFiles;
		}
	}

	@Override
	public LoaderPlugin originPlugin() {
		return this.plugin;
	}

	@Override
	public Map<String, T> getMods() {
		if(this.mods == null) {
			this.mods = new HashMap<>();
			List<ModFiles> files = this.getFiles();
			//noinspection ForLoopReplaceableByForEach
			for(int i = 0; i < files.size(); i++) {
				ModFiles file = files.get(i);
				this.loadMod(file);
			}
		}
		return this.mods;
	}

	protected void loadMod(ModFiles files) {
		T meta;
		try {
			meta = this.getMetadata(files);
		} catch(IOException e) {
			throw Main.rethrow(e);
		}
		if(meta != null) {
			T old = this.mods.get(meta.id());
			if(old != null) {
				T replace = this.redundantMod(old, meta);
				if(replace != old) {
					this.mods.put(meta.id(), meta);
				}
			} else {
				this.mods.put(meta.id(), meta);
			}
		}
	}

	@Override
	public void close() throws Exception {
		if(this.resolvedFiles != null) {
			for(ModFiles file : this.resolvedFiles) {
				file.close();
			}
		}
	}

	protected abstract List<ModFiles> resolveModFiles() throws IOException;

	@Nullable
	protected abstract T getMetadata(ModFiles path) throws IOException;

	protected T redundantMod(T a, T b) {
		LOGGER.warning("Multiple mods with id " + a.id() + " choosing a random one!");
		return a;
	}

	protected void proposeFile(ModFiles files) {
		this.getFiles().add(files);
	}
}
