package com.maldloader.v0.api.plugin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.modloader.ModLoader;
import com.maldloader.v0.api.LoaderList;

public interface LoaderPlugin {
	default void init() {}

	default void offerModLoaders(Consumer<ModLoader<?>> loaderConsumer) {}

	/**
	 * offer main classes, maldloader will choose a main class based on an environment arg, the string passed as an id becomes the string required in arg
	 */
	default void offerMainClasses(LoaderList mald, MainClassLoader loader, BiConsumer<String, Class<?>> idAndClassConsumer) {}

	default void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {}
}
