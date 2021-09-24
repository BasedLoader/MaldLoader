package io.github.mald;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.gson.Gson;
import io.github.mald.impl.classloader.DynUrlLoader;
import io.github.mald.impl.classloader.Main;
import io.github.mald.impl.classloader.ModClassLoader;
import io.github.mald.mixin.MaldMixinBootstrap;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.NullClassLoader;
import io.github.mald.v0.api.classloader.DefaultChildClassLoader;
import io.github.mald.v0.api.classloader.MainClassLoader;
import io.github.mald.v0.api.modloader.AbstractModLoader;
import io.github.mald.v0.api.modloader.ModFiles;
import io.github.mald.v0.api.plugin.LoaderPlugin;
import org.jetbrains.annotations.Nullable;

public class MaldLoader extends AbstractModLoader<MaldMod> {
	public static final Path INCLUDES = Paths.get("mods", "includes");
	public static final Logger LOGGER = LogManager.getLogManager().getLogger("MaldLoader-Minecraft");
	public static final Gson GSON = new Gson();

	public MaldLoader(LoaderPlugin plugin) {
		super(plugin);
	}

	@Override
	public void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {
		DynUrlLoader urlClassLoader = new DynUrlLoader(new URL[0]);
		List<ModFiles> files = this.getFiles();
		for(ModFiles file : files) {
			for(Path path : file.files) {
				urlClassLoader.addURL(path.toUri().toURL());
			}
		}

		ModClassLoader mod = new ModClassLoader(NullClassLoader.INSTANCE, urlClassLoader);

		// mod transformation can be done here

		DefaultChildClassLoader classLoader = new DefaultChildClassLoader(loader, mod);
		loader.offer(classLoader);

		MaldMixinBootstrap.loadMixinMods(this.getMods().values());
	}

	@Override
	protected List<ModFiles> resolveModFiles() {
		return Main.getPathsViaProperty("mald.loader", "mods");
	}

	@Override
	protected @Nullable MaldMod getMetadata(ModFiles path) throws IOException {
		Path json = path.resolveExists("mod.mald.json");
		if(json == null) {
			return null;
		}
		try(Reader reader = Files.newBufferedReader(json)) {
			MaldMod mod = GSON.fromJson(reader, MaldMod.class);
			mod.files = path;
			if(mod.include != null) {
				for(String included : mod.include) {
					Path jijed = path.resolveExists(included);
					if(jijed == null) {
						throw new FileNotFoundException("file "+included+" in mod " + mod.id() + " not found!");
					}
					Path extracted = INCLUDES.resolve(included);
					Files.createDirectories(extracted.getParent());
					Files.copy(jijed, extracted);
					this.proposeFile(ModFiles.autoDetect(extracted));
				}
			}
			return mod;
		}
	}

	@Override
	protected MaldMod redundantMod(MaldMod a, MaldMod b) {
		Version av = Version.parseVersion(a.getVersion(), true), bv = Version.parseVersion(b.getVersion(), true);
		if(av.equals(bv)) {
			LOGGER.warning("Two mods with version " + a.getVersion() + " choosing a random one!");
		}
		return av.compareTo(bv) > 0 ? a : b;
	}
}
