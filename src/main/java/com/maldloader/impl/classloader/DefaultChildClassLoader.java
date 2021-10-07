package com.maldloader.impl.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.maldloader.v0.api.classloader.ExtendedClassLoader;
import com.maldloader.v0.api.classloader.MainClassLoader;
import org.jetbrains.annotations.Nullable;

public class DefaultChildClassLoader extends ExtendedClassLoader.Secure {
	static {
		registerAsParallelCapable();
	}

	final MainClassLoader main;
	ClassLoader loader;
	ExtendedClassLoader access;

	public DefaultChildClassLoader(MainClassLoader main) {
		super(main.instance());
		this.main = main;
	}

	public DefaultChildClassLoader set(ClassLoader loader, ExtendedClassLoader access) {
		this.loader = loader;
		this.access = access;
		return this;
	}

	public <C extends ClassLoader & ExtendedClassLoader> DefaultChildClassLoader set(C loader) {
		return this.set(loader, loader);
	}

	public @Nullable Class<?> searchClass(String name, boolean resolve) {
		Class<?> val;
		try {
			if(resolve) {
				val = this.access.loadClass0(name, true);
			} else {
				val = this.loader.loadClass(name);
			}
		} catch(ClassNotFoundException e) {
			val = null;
		}

		return val;
	}

	public @Nullable URL searchResource(String name) {
		return this.loader.getResource(name);
	}

	public @Nullable Enumeration<URL> searchResources(String name) {
		try {
			return this.loader.getResources(name);
		} catch(IOException e) {
			throw Main.rethrow(e);
		}
	}

	public @Nullable InputStream searchStream(String name) {
		return this.loader.getResourceAsStream(name);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> cls = this.main.findClass(name, resolve, this);
		if(cls == null) {
			throw new ClassNotFoundException(name);
		}
		return cls;
	}

	@Nullable
	@Override
	public URL getResource(String name) {
		return this.main.getResource(name, this);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return this.main.getResources(name, this);
	}

	@Nullable
	@Override
	public InputStream getResourceAsStream(String name) {
		return this.main.getResourceAsStream(name, this);
	}
}
