package io.github.mald.v0.api.classloader;

import java.security.SecureClassLoader;

import io.github.mald.impl.classloader.Main;

public interface ExtendedClassLoader {
	boolean isClassLoaded(String name);

	abstract class Secure extends SecureClassLoader implements ExtendedClassLoader {
		public Secure(ClassLoader parent) {
			super(parent);
		}

		public Secure() {
		}

		@Override
		public Class<?> findClass(String name) {
			try {
				return super.findClass(name);
			} catch(ClassNotFoundException e) {
				throw Main.rethrow(e);
			}
		}

	}
}
