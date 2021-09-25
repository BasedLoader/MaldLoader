package com.maldloader.static_injector;

import com.maldloader.v0.api.LoaderList;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.modloader.ModLoader;
import com.maldloader.v0.api.modloader.ModMetadata;
import com.maldloader.v0.api.plugin.LoaderPlugin;

public class StaticInjectorPlugin implements LoaderPlugin {
	@Override
	public void afterModLoaderInit(LoaderList loader, MainClassLoader classLoader) {
		LoaderPlugin.super.afterModLoaderInit(loader, classLoader);
		for(ModLoader<?> modLoader : loader.modLoaders) {
			for(ModMetadata value : modLoader.getMods().values()) {
			}
		}
	}
}
