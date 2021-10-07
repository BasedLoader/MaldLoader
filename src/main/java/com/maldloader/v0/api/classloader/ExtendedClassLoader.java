package com.maldloader.v0.api.classloader;

import java.security.SecureClassLoader;

import com.maldloader.impl.classloader.Main;

public interface ExtendedClassLoader {
	boolean isClassLoaded(String name);

	/**
	 * Calls {@link ClassLoader#loadClass(String, boolean)}
	 */
	Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException;

	abstract class Secure extends SecureClassLoader implements ExtendedClassLoader {
		public Secure(ClassLoader parent) {
			super(parent);
		}

		public Secure() {
		}

		@Override
		public boolean isClassLoaded(String name) {
			synchronized(this.getClassLoadingLock(name)) {
				return this.findLoadedClass(name) != null;
			}
		}

		@Override
		public Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException {
			return this.loadClass(name, resolve);
		}
	}
}
