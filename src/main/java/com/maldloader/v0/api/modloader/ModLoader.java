package com.maldloader.v0.api.modloader;

import java.net.MalformedURLException;
import java.util.Map;

import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.LoaderList;
import com.maldloader.v0.api.plugin.LoaderPlugin;

public interface ModLoader<T extends ModMetadata> {
	LoaderPlugin originPlugin();

	Map<String, T> getMods();

	default void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {}
}
