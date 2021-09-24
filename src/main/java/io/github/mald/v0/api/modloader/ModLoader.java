package io.github.mald.v0.api.modloader;

import java.net.MalformedURLException;
import java.util.Map;

import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;

public interface ModLoader<T extends ModMetadata> {
	LoaderPlugin originPlugin();

	Map<String, T> getMods();

	default void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {}
}
