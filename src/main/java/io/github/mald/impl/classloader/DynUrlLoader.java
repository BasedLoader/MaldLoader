package io.github.mald.impl.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class DynUrlLoader extends URLClassLoader {
	public DynUrlLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public DynUrlLoader(URL[] urls) {
		super(urls);
	}

	public DynUrlLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
}
