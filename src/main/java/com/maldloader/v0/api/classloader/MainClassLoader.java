package com.maldloader.v0.api.classloader;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import com.maldloader.v0.api.transformer.BufferTransformer;
import com.maldloader.v0.api.transformer.LazyDefiner;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import com.maldloader.v0.api.transformer.asm.ClassVisitorTransformer;
import com.maldloader.v0.api.transformer.asm.WriterFlagGetter;
import org.jetbrains.annotations.Nullable;

public interface MainClassLoader extends ExtendedClassLoader {
	/**
	 * @param definer adds a lazy class that is evaluated before the parent classloader is checked for if a class exists
	 *  this is useful when trying to override a library class for whatever reason
	 */
	void addPreParentDefiner(LazyDefiner definer);

	/**
	 * @param definer adds a lazy class that is evaluated after the parent classloader is checked for if a class exists
	 *  this is what you should probably use for most of the use cases of a lazy definer
	 */
	void addPostParentDefiner(LazyDefiner definer);

	void addTransformer(BufferTransformer transformer);

	void addVisitorTransformer(ClassVisitorTransformer transformer);

	void addClassNodeTransformer(ClassNodeTransformer transformer);

	/**
	 * This is necessary if u want to compute max or compute frames, do not always return max or frames though
	 */
	void addWriterFlags(WriterFlagGetter getter);

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
