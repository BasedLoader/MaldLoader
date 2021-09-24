package io.github.mald;

import java.nio.file.Path;
import java.util.List;

import io.github.mald.mixin.MixinModMetadata;
import io.github.mald.v0.api.modloader.ModFiles;
import io.github.mald.v0.api.modloader.ModMetadata;

public class MaldMod extends ModMetadata.Standard implements MixinModMetadata {
	List<String> dependency, include;
	List<String> mixins;
	ModFiles files;

	public List<String> getDependency() {
		return this.dependency;
	}

	public List<String> getInclude() {
		return this.include;
	}

	public ModFiles getPath() {
		return this.files;
	}

	@Override
	public List<String> mixinFiles() {
		return this.mixins;
	}
}
