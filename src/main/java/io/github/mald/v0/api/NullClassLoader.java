package io.github.mald.v0.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import org.jetbrains.annotations.Nullable;

public final class NullClassLoader extends ClassLoader {
	public static final NullClassLoader INSTANCE = new NullClassLoader();
	private NullClassLoader() {
		super(null);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException();
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		throw new ClassNotFoundException();
	}

	@Override
	protected Object getClassLoadingLock(String className) {
		return null;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException();
	}

	@Nullable
	@Override
	public URL getResource(String name) {
		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return Collections.emptyEnumeration();
	}

	@Override
	protected URL findResource(String name) {
		return null;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return Collections.emptyEnumeration();
	}

	@Nullable
	@Override
	public InputStream getResourceAsStream(String name) {
		return null;
	}

	@Override
	protected Package definePackage(String name,
			String specTitle,
			String specVersion,
			String specVendor,
			String implTitle,
			String implVersion,
			String implVendor,
			URL sealBase) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Package getPackage(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Package[] getPackages() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String findLibrary(String libname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultAssertionStatus(boolean enabled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPackageAssertionStatus(String packageName, boolean enabled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setClassAssertionStatus(String className, boolean enabled) {
		throw new UnsupportedOperationException();
	}
}
