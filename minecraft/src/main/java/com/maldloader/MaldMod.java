package com.maldloader;

import java.util.List;

import com.maldloader.mixin.MixinModMetadata;
import com.maldloader.v0.api.modloader.ModFiles;
import com.maldloader.v0.api.modloader.ModMetadata;

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
