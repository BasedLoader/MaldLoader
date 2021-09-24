package com.maldloader.testmod;

import com.maldloader.MinecraftPlugin;

public class MainTest {

	public static void main(String[] args) throws Throwable {
		System.setProperty(LoaderPluginLoader.MALD + ".main", "test");
		MinecraftPlugin.main(args);
	}
}
