package io.github.mald.impl.classloader;

import org.jetbrains.annotations.Nullable;

/**
 * If a class is not found in the jars of the mod class loader, the classloader delegates to this interface, which allows it to generate classes based on the class name.
 */
public interface LazyDefiner {
	@Nullable
	Buf forName(String name);
}
