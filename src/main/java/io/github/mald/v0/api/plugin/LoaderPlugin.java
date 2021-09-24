package io.github.mald.v0.api.plugin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.ModLoader;

public interface LoaderPlugin {
	default void init() {}

	default void offerModLoaders(Consumer<ModLoader<?>> loaderConsumer) {}

	/**
	 * offer main classes, maldloader will choose a main class based on an environment arg, the string passed as an id becomes the string required in arg
	 */
	default void offerMainClasses(LoaderList mald, MainClassLoader loader, BiConsumer<String, Class<?>> idAndClassConsumer) {}

	default void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {}
}
