package com.maldloader.v0.api.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	final Access access;

	public DefaultChildClassLoader(MainClassLoader main, ClassLoader loader, Access access) {
		super(main.instance());
		if(loader.getParent() != NullClassLoader.INSTANCE) {
			throw new IllegalArgumentException(
					"Loader must use NullClassLoader as a parent! This is to prevent you from accidentally loading resources from the system " +
					"classloader");
		}
		this.loader = loader;
		this.main = main;
		this.access = access;
	}

	public <C extends ClassLoader & Access> DefaultChildClassLoader(MainClassLoader main, C loader) {
		this(main, loader, loader);
	}

	public <C extends ClassLoader> DefaultChildClassLoader(MainClassLoader main, C loader, ContextedAccess access) {
		this(main, loader, (n, r) -> access.loadClass(loader, n, r));
	}

	public interface Access {
		Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException;
	}

	public interface ContextedAccess {
		Class<?> loadClass(ClassLoader loader, String name, boolean resolve) throws ClassNotFoundException;
	}

	@Override
	public @Nullable Class<?> searchClass(String name, boolean resolve) {
		try {
			if(resolve) {
				return this.access.loadClass0(name, true);
			} else {
				return this.loader.loadClass(name);
			}
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
