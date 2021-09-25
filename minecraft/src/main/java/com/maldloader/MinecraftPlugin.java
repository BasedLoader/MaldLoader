package com.maldloader;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.maldloader.impl.classloader.Main;
import com.maldloader.mixin.MaldMixinService;
import com.maldloader.v0.api.LoaderList;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.modloader.ModFiles;
import com.maldloader.v0.api.modloader.ModLoader;
import com.maldloader.v0.api.plugin.LoaderPlugin;
import com.maldloader.v0.api.transformer.asm.ClassHeader;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public class MinecraftPlugin implements LoaderPlugin {
	private static final Logger LOGGER = Logger.getLogger("MaldLoader/Minecraft");
	public static final boolean IS_CLIENT = Boolean.getBoolean("mald.mc.isClient");

	@Override
	public void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {
		classLoader.addClassNodeTransformer(new ClassNodeTransformer() {
			@Override
			public void accept(ClassNode node) {
				IMixinTransformer transformer = MaldMixinService.service.transformer;
				transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), node.name.replace('/', '.'), node);
			}

			@Override
			public boolean transforms(ClassHeader header) {
				return true;
			}
		});


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
