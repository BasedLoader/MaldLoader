package com.maldloader;

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
import com.maldloader.v0.api.classloader.DynURLClassLoader;
import com.maldloader.impl.classloader.Main;
import com.maldloader.v0.api.LoaderList;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.modloader.AbstractModLoader;
import com.maldloader.v0.api.modloader.ModFiles;
import com.maldloader.v0.api.plugin.LoaderPlugin;
import org.jetbrains.annotations.Nullable;

public class MaldLoader extends AbstractModLoader<MaldMod> {
	public static final Path INCLUDES = Paths.get("mods", "includes");
	public static final Logger LOGGER = Logger.getLogger("MaldLoader/Minecraft");
	public static final Gson GSON = new Gson();

	public MaldLoader(LoaderPlugin plugin) {
		super(plugin);
	}

	@Override
	public void init(LoaderList maldLoader, MainClassLoader loader) throws MalformedURLException {
		DynURLClassLoader urlClassLoader = new DynURLClassLoader(new URL[0]);
		List<ModFiles> files = this.getFiles();
		for(ModFiles file : files) {
			for(Path path : file.files) {
				loader.offer(path.toUri().toURL());
			}
		}
		LOGGER.info("Loaded " + files.size() + " mods!");

		//loader.offerWrapped(urlClassLoader);
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
