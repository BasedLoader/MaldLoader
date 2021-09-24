package io.github.mald.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import io.github.mald.impl.classloader.ModClassLoader;
import io.github.mald.v0.api.modloader.AbstractModLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;
import io.github.mald.v0.api.modloader.ModMetadata;

public class LoaderPluginLoader extends AbstractModLoader<LoaderPluginLoader.Meta> {
	public static final String MALD = "mald";
	final List<Path> paths;

	public LoaderPluginLoader(List<Path> paths) {this.paths = paths;}

	/**
	 * @return nothing, because it throws
	 * @throws T rethrows {@code throwable}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
		throw (T) throwable;
	}

	@Override
	protected List<Path> resolveMods() {
		return this.paths;
	}

	@Override
	protected Meta extractMetadata(Path path, FileSystem system) throws IOException {
		Path mald = system.getPath(MALD + ".properties");
		if(Files.exists(mald)) {
			Properties properties = new Properties();
			try(BufferedReader reader = Files.newBufferedReader(mald)) {
				properties.load(reader);
			}
			String id = Objects.requireNonNull(properties.getProperty("modid")), init = Objects.requireNonNull(properties.getProperty("init"));
			return new Meta(path, id, init);
		}
		return null;
	}

	public List<LoaderPlugin> init(ClassLoader parent) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<Meta> metas = this.getMods();
		URL[] urls = new URL[metas.size()];
		for(int i = 0; i < metas.size(); i++) {
			Meta mod = metas.get(i);
			urls[i] = mod.path.toUri().toURL();
		}
		URLClassLoader loader = new URLClassLoader(urls, null);
		ModClassLoader mods = new ModClassLoader(parent, loader);
		List<LoaderPlugin> plugins = new ArrayList<>();
		for(Meta meta : metas) {
			Class<?> cls = Class.forName(meta.pluginClass, false, mods);
			if(LoaderPlugin.class.isAssignableFrom(cls)) {
				LoaderPlugin plugin = (LoaderPlugin) cls.newInstance();
				meta.plugin = plugin;
				plugin.init();
				plugins.add(plugin);
			} else {
				throw new UnsupportedOperationException(cls + " does not implement MaldPlugin");
			}
		}
		return plugins;
	}

	public static class Meta implements ModMetadata {
		final Path path;
		final String id;
		final String pluginClass;
		LoaderPlugin plugin;

		public Meta(Path path, String id, String aClass) {
			this.path = path;
			this.id = id;
			this.pluginClass = aClass;}

		@Override
		public String id() {
			return this.id;
		}
	}
}
