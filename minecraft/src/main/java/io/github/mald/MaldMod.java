package io.github.mald;

import java.nio.file.Path;
import java.util.List;

import io.github.mald.v0.api.modloader.ModMetadata;

public class MaldMod extends ModMetadata.Standard {
	List<String> dependency, include;
	Path path;

	public List<String> getDependency() {
		return this.dependency;
	}

	public List<String> getInclude() {
		return this.include;
	}

	public Path getPath() {
		return this.path;
	}
}
