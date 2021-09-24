package com.maldloader.v0.api.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Enumeration;

import com.maldloader.impl.classloader.Main;
import com.maldloader.v0.api.NullClassLoader;
import org.jetbrains.annotations.Nullable;

public class DefaultChildClassLoader extends SecureClassLoader implements ChildClassLoader {
	static {
		registerAsParallelCapable();
	}

	final MainClassLoader main;
	final ClassLoader loader;

	public DefaultChildClassLoader(MainClassLoader main, ClassLoader loader) {
		super(main.instance());
		if(loader.getParent() != NullClassLoader.INSTANCE) {
			throw new IllegalArgumentException("Loader must use NullClassLoader as a parent! This is to prevent you from accidentally loading resources from the system classloader");
		}
		this.loader = loader;
		this.main = main;
	}

	@Override
	public @Nullable Class<?> searchClass(String name, boolean resolve) {
		try {
			return this.loader.loadClass(name);
		} catch(ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public @Nullable URL searchResource(String name) {
		return this.loader.getResource(name);
	}

	@Override
	public @Nullable Enumeration<URL> searchResources(String name) {
		try {
			return this.loader.getResources(name);
		} catch(IOException e) {
			throw Main.rethrow(e);
		}
	}

	@Override
	public @Nullable InputStream searchStream(String name) {
		return this.loader.getResourceAsStream(name);
	}

	@Override
	public boolean isClassLoaded(String name) {
		synchronized(this.getClassLoadingLock(name)) {
			return this.findLoadedClass(name) != null;
		}
	}
}
