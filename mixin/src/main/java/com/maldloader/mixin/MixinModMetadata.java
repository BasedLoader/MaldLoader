package com.maldloader.mixin;

import java.util.List;

import com.maldloader.v0.api.modloader.ModMetadata;

public interface MixinModMetadata extends ModMetadata {
	/**
	 * @return empty if no mixin files
	 */
	List<String> mixinFiles();
}
