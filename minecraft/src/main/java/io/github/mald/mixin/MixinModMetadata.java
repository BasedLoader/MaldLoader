package io.github.mald.mixin;

import java.util.List;

import io.github.mald.v0.api.modloader.ModMetadata;

public interface MixinModMetadata extends ModMetadata {
	/**
	 * @return empty if no mixin files
	 */
	List<String> mixinFiles();
}
