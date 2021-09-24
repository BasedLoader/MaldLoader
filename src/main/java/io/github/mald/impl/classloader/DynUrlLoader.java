package io.github.mald.impl.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import io.github.mald.v0.api.NullClassLoader;

public class DynUrlLoader extends URLClassLoader {
	public DynUrlLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public DynUrlLoader(URL[] urls) {
		super(urls, NullClassLoader.INSTANCE);
	}

	public DynUrlLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
}
