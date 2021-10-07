package com.maldloader.v0.api.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import com.maldloader.v0.api.NullClassLoader;

public class DynURLClassLoader extends URLClassLoader {
	public DynURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public DynURLClassLoader(URL[] urls) {
		super(urls, NullClassLoader.INSTANCE);
	}

	public DynURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}
}
