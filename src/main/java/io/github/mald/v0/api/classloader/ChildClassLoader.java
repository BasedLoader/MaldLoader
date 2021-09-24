package io.github.mald.v0.api.classloader;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.jetbrains.annotations.Nullable;

public interface ChildClassLoader extends ExtendedClassLoader {
	/**
	 * Try to find the class without querying the parent (main) class loader
	 */
	@Nullable
	Class<?> searchClass(String name, boolean resolve);

	/**
	 * Try to find the resource without querying the parent (main) class loader
	 */
	@Nullable
	URL searchResource(String name);

	@Nullable
	Enumeration<URL> searchResources(String name);

	@Nullable
	InputStream searchStream(String name);
}
