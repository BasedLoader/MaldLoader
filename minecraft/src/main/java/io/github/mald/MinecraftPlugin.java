package io.github.mald;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.github.mald.impl.classloader.Main;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.ModFiles;
import io.github.mald.v0.api.modloader.ModLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;

public class MinecraftPlugin implements LoaderPlugin {
	private static final Logger LOGGER = Logger.getLogger("MaldLoader/Minecraft");
	public static final boolean IS_CLIENT = Boolean.getBoolean("mald.mc.isClient");

	@Override
	public void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {
		for(ModFiles path : Main.getPathsViaProperty("mald.mc", null)) {
			try {
				path.addTo(classLoader);
			} catch(MalformedURLException e) {
				throw Main.rethrow(e);
			}
		}
	}

	@Override
	public void offerModLoaders(Consumer<ModLoader<?>> loaderConsumer) {
		loaderConsumer.accept(new MaldLoader(this));
	}

	@Override
	public void offerMainClasses(LoaderList mald, MainClassLoader loader, BiConsumer<String, Class<?>> idAndClassConsumer) {
		List<String> mainClass;
		String id;
		if (IS_CLIENT) {
			id = "client";
			mainClass = Arrays.asList("net.minecraft.client.main.Main", "net.minecraft.client.MinecraftApplet", "com.mojang.minecraft.MinecraftApplet");
		} else {
			id = "server";
			mainClass = Arrays.asList("net.minecraft.server.Main", "net.minecraft.server.MinecraftServer", "com.mojang.minecraft.server.MinecraftServer");
		}
		for(String className : mainClass) {
			Class<?> type = loader.findClass(className, null);
			if(type != null) {
				idAndClassConsumer.accept(id, type);
				return;
			}
		}
		LOGGER.warning("Unable to locate Main class!");
	}

	public static void main(String[] args) throws Throwable {
		Main.main(args);
	}
}
