package com.maldloader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.maldloader.impl.classloader.MainClassLoaderImpl;
import com.maldloader.impl.classloader.ModClassLoader;
import com.maldloader.v0.api.modloader.AbstractModLoader;
import com.maldloader.v0.api.modloader.ModFiles;
import com.maldloader.v0.api.plugin.LoaderPlugin;
import com.maldloader.v0.api.modloader.ModMetadata;
import org.jetbrains.annotations.Nullable;

public class LoaderPluginLoader extends AbstractModLoader<LoaderPluginLoader.Meta> {
	public static final String MALD = "mald";
	final List<ModFiles> paths;

	public LoaderPluginLoader(List<ModFiles> paths) {
		super(null);
		this.paths = paths;
	}

	@Override
	protected List<ModFiles> resolveModFiles() {
		return this.paths;
	}

	@Override
	protected @Nullable LoaderPluginLoader.Meta getMetadata(ModFiles path) throws IOException {
		Path mald = path.resolveExists(MALD + ".properties");
		if(mald != null) {
			Properties properties = new Properties();
			try(BufferedReader reader = Files.newBufferedReader(mald)) {
				properties.load(reader);
			}
			String id = Objects.requireNonNull(properties.getProperty("modid")), init = Objects.requireNonNull(properties.getProperty("init"));
			return new Meta(path, id, init);
		}
		return null;
	}


	public Map<String, LoaderPlugin> init(ClassLoader parent, ModClassLoader[] ref) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Collection<Meta> metas = this.getMods().values();
		MainClassLoaderImpl.DynURLClassLoader loader = new MainClassLoaderImpl.DynURLClassLoader(new URL[0]);
		for(ModFiles file : this.getFiles()) {
			for(Path path : file.files) {
				loader.addURL(path.toUri().toURL());
			}
		}
		ModClassLoader mods = new ModClassLoader(parent, loader);
		ref[0] = mods;
		Map<String, LoaderPlugin> plugins = new HashMap<>();
		for(Meta meta : metas) {
			Class<?> cls = Class.forName(meta.pluginClass, false, mods);
			if(LoaderPlugin.class.isAssignableFrom(cls)) {
				LoaderPlugin plugin = (LoaderPlugin) cls.newInstance();
				meta.plugin = plugin;
				plugin.init();
				plugins.put(meta.id, plugin);
			} else {
				throw new UnsupportedOperationException(cls + " does not implement LoaderPlugin");
			}
		}
		return plugins;
	}

	public static class Meta implements ModMetadata {
		final ModFiles path;
		final String id;
		final String pluginClass;
		LoaderPlugin plugin;

		public Meta(ModFiles path, String id, String aClass) {
			this.path = path;
			this.id = id;
			this.pluginClass = aClass;}

		@Override
		public String id() {
			return this.id;
		}

		@Override
		public String name() {
			return this.id;
		}

		@Override
		public String description() {
			return "loader plugin";
		}
	}
}
