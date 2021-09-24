package io.github.mald.v0.api.classloader;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.jetbrains.annotations.Nullable;

public interface MainClassLoader extends ExtendedClassLoader {
	Class<?> define(String name, byte[] buf, int off, int len);

	/**
	 * Adds a URL to the main classloader
	 */
	void offer(URL url);

	<T extends ClassLoader & ChildClassLoader> void offer(T loader);

	/**
	 * @return checks if the class is loaded in all offered classloaders (except for the passed classloader) and itself
	 */
	boolean isClassLoaded(String name, @Nullable ClassLoader except);

	@Nullable
	default Class<?> findClass(String name, @Nullable ClassLoader except) {
		return this.findClass(name, false, except);
	}

	/**
	 * @return attempts to find the class in all offered classloaders (except for the passed classloader) and itself
	 */
	@Nullable
	Class<?> findClass(String name, boolean resolve, @Nullable ClassLoader except);

	URL getResource(String name, @Nullable ClassLoader except);

	Enumeration<URL> getResources(String name, @Nullable ClassLoader except);

	InputStream getResourceAsStream(String name, @Nullable ClassLoader except);

	/**
	 * @return This classloader
	 */
	ClassLoader instance();


}
