package io.github.mald.v0.api.transformer;

import org.jetbrains.annotations.Nullable;

/**
 * If a class is not found in the jars of the mod class loader, the classloader delegates to this interface,
 * this allows it to generate classes based on the class name.
 */
public interface LazyDefiner {
	@Nullable
	Buf forName(String name);
}
