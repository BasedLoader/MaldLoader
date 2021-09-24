package io.github.mald.v0.api.plugin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.ModLoader;

public interface LoaderPlugin {
	default void init() {}

	default void offerModLoaders(Consumer<ModLoader<?>> loaderConsumer) {}

	default void offerMainClasses(MainClassLoader loader, BiConsumer<String, Class<?>> cls) {}
}
