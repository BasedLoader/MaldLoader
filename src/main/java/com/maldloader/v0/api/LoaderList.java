package com.maldloader.v0.api;

import java.util.List;
import java.util.Map;

import com.maldloader.impl.classloader.Main;
import com.maldloader.v0.api.modloader.ModLoader;
import com.maldloader.v0.api.plugin.LoaderPlugin;

public class LoaderList {
	public final Map<String, LoaderPlugin> plugins;
	public final List<ModLoader<?>> modLoaders;

	public LoaderList(Map<String, LoaderPlugin> plugins, List<ModLoader<?>> loaders) {
		this.plugins = plugins;
		this.modLoaders = loaders;
	}

	public LoaderPlugin getById(String key) {
		return this.plugins.get(key);
	}

	public <T> T byClass(Class<T> type) {
		for(LoaderPlugin value : this.plugins.values()) {
			if(type.isInstance(value)) {
				return (T) value;
			}
		}
		throw Main.rethrow(new ClassNotFoundException("loader with type " + type));
	}
}
