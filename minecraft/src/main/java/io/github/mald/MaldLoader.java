package io.github.mald;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.gson.Gson;
import io.github.mald.impl.classloader.Main;
import io.github.mald.impl.classloader.ModClassLoader;
import io.github.mald.mixin.MaldMixinBootstrap;
import io.github.mald.mixin.MixinModMetadata;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.NullClassLoader;
import io.github.mald.v0.api.classloader.DefaultChildClassLoader;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.AbstractModLoader;
import io.github.mald.v0.api.modloader.ModMetadata;
import io.github.mald.v0.api.plugin.LoaderPlugin;

public class MaldLoader extends AbstractModLoader<MaldMod> {
	public static final Path INCLUDES = Paths.get("mods", "includes");
	public static final Logger LOGGER = LogManager.getLogManager().getLogger("MaldLoader-Minecraft");
	public static final Gson GSON = new Gson();

	public MaldLoader(LoaderPlugin plugin) {
		super(plugin);
	}

	@Override
	protected List<Path> resolveMods() {
		return Main.getPathsViaProperty("mald.mc", "mods");
	}

	@Override
	public void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {
		List<Path> files = this.getModFiles();
		URL[] urls = new URL[files.size()];
		for(int i = 0, size = files.size(); i < size; i++) {
			Path mod = files.get(i);
			urls[i] = mod.toUri().toURL();
		}
		URLClassLoader urlClassLoader = new URLClassLoader(urls);
		ModClassLoader mod = new ModClassLoader(NullClassLoader.INSTANCE, urlClassLoader);
		DefaultChildClassLoader classLoader = new DefaultChildClassLoader(loader, mod);
		loader.offer(classLoader);
	}

	@Override
	protected MaldMod extractMetadata(Path path, FileSystem system) throws IOException {
		Path json = system.getPath("mod.mald.json");
		if(!Files.exists(json)) return null;
		try(Reader reader = Files.newBufferedReader(json)) {
			MaldMod mod = GSON.fromJson(reader, MaldMod.class);
			mod.path = path;
			for(String included : mod.include) {
				Path jijed = system.getPath(included);
				Path extracted = INCLUDES.resolve(included);
				Files.createDirectories(extracted.getParent());
				Files.copy(jijed, extracted);
				FileSystem sys = FileSystems.newFileSystem(extracted, (ClassLoader) null);
				this.systems.add(sys);
				this.modFiles.add(jijed);
			}
			return mod;
		}
	}

	@Override
	protected MaldMod onModIdOverride(MaldMod a, MaldMod b) {
		Version av = Version.parseVersion(a.getVersion(), true), bv = Version.parseVersion(b.getVersion(), true);
		if(av.equals(bv)) {
			LOGGER.warning("Two mods with version " + a.getVersion() + " choosing a random one!");
		}
		return av.compareTo(bv) > 0 ? a : b;
	}

	@Override
	protected void initializeMods() {
		MaldMixinBootstrap.loadMixinMods(this.mods.values());
	}
}
