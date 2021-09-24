package io.github.mald.impl;

import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.ModLoader;
import io.github.mald.v0.api.modloader.ModMetadata;
import io.github.mald.v0.api.plugin.LoaderPlugin;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Default Loader Plugin. should be able to load mods from the mods directors and nothing more.
 */
public class DefaultLoaderPlugin implements LoaderPlugin, ModLoader<ModMetadata> {

	@Override
	public void offerModLoaders(Consumer<ModLoader<?>> loaderConsumer) {
		loaderConsumer.accept(this);
	}

	@Override
	public void offerMainClasses(MainClassLoader loader, BiConsumer<String, Class<?>> cls) {
		// TODO: outside of dev & other mapping platform support
		System.out.println("Minecraft is Starting...");
		try {
			cls.accept("test", Class.forName("net.minecraft.client.main.Main"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<ModMetadata> getMods() {
		return List.of(new ModMetadata() {
			@Override
			public String id() {
				return "default";
			}

			@Override
			public String name() {
				return "Default Loader";
			}

			@Override
			public String description() {
				return "The default mod loader";
			}

			@Override
			public String mixinFile() {
				return "examplemod.mixins.json";
			}
		});
	}
}
