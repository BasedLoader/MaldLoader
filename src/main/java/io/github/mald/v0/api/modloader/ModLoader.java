package io.github.mald.v0.api.modloader;

import java.net.MalformedURLException;
import java.util.List;

import io.github.mald.v0.api.classloader.MainClassLoader;

public interface ModLoader<T extends ModMetadata> {
	List<T> getMods();

	default void init(MainClassLoader loader) throws MalformedURLException {}
}
