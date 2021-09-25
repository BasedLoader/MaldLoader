package com.maldloader.impl.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiFunction;

import com.maldloader.v0.api.NullClassLoader;
import com.maldloader.v0.api.classloader.ChildClassLoader;
import com.maldloader.v0.api.classloader.ExtendedClassLoader;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.transformer.BufferTransformer;
import com.maldloader.v0.api.transformer.LazyDefiner;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import com.maldloader.v0.api.transformer.asm.ClassVisitorTransformer;
import com.maldloader.v0.api.transformer.asm.ReaderFlagGetter;
import org.jetbrains.annotations.Nullable;

public class MainClassLoaderImpl extends SecureClassLoader implements MainClassLoader {

	public static MainClassLoaderImpl instance;

	static {
		registerAsParallelCapable();
	}

	final DynURLClassLoader modJarContainer;
	final ModClassLoader mainLoader;
	final List<ClassLoader> loaders = new ArrayList<>();
	final MultiBufferTransformer transformer = new MultiBufferTransformer();
	final AsmTransformerHelper helper = new AsmTransformerHelper();
	final MultiLazyDefiner pre = new MultiLazyDefiner(), post = new MultiLazyDefiner();

	public MainClassLoaderImpl(ClassLoader parent) {
		super(new ModClassLoader(parent, new DynURLClassLoader(new URL[] {})));
		this.mainLoader = (ModClassLoader) this.getParent();
		this.modJarContainer = (DynURLClassLoader) (this.mainLoader.mods);
		this.transformer.transformers.add(this.helper);
		this.mainLoader.setTransformer(this.transformer);
		this.mainLoader.setPreParent(this.pre);
		this.mainLoader.setPostParent(this.post);
		instance = this;
	}

	@Override
	public boolean isClassLoaded(String name) {
		return this.isClassLoaded(name, null);
	}

	@Override
	public void addPreParentDefiner(LazyDefiner definer) {
		this.pre.definers.add(definer);
	}

	@Override
	public void addPostParentDefiner(LazyDefiner definer) {
		this.post.definers.add(definer);
	}

	@Override
	public void addTransformer(BufferTransformer transformer) {
		this.transformer.transformers.add(transformer);
	}

	@Override
	public void addVisitorTransformer(ClassVisitorTransformer transformer) {
		this.helper.visit.add(transformer);
	}

	@Override
	public void addClassNodeTransformer(ClassNodeTransformer transformer) {
		this.helper.node.add(transformer);
	}

	@Override
	public void addReaderFlagGetter(ReaderFlagGetter getter) {
		this.helper.flags.add(getter);
	}

	@Override
	public Class<?> define(String name, byte[] buf, int off, int len) {
		return this.mainLoader.define(name, buf, off, len);
	}

	@Override
	public void offer(URL url) {
		this.modJarContainer.addURL(url);
	}

	@Override
	public void offer(ClassLoader loader) {
		this.loaders.add(loader);
	}

	@Override
	public boolean isClassLoaded(String name, @Nullable ClassLoader except) {
		return Boolean.TRUE.equals(this.get(name, except, ExtendedClassLoader::isClassLoaded, ModClassLoader::isClassLoaded));
	}

	@Override
	public @Nullable Class<?> findClass(String name, boolean resolve, @Nullable ClassLoader except) {
		synchronized(this.getClassLoadingLock(name)) {
			return this.get(name, except, (c, n) -> c.searchClass(n, resolve), (c, n) -> c.loadClass(n, resolve));
		}
	}

	@Override
	public URL getResource(String name, @Nullable ClassLoader except) {
		return this.get(name, except, ChildClassLoader::searchResource, ClassLoader::getResource);
	}

	@Override
	public Enumeration<URL> getResources(String name, @Nullable ClassLoader except) {
		Enumeration<URL> enumeration = this.get(name, except, ChildClassLoader::searchResources, ClassLoader::getResources);
		return enumeration == null ? Collections.emptyEnumeration() : enumeration;
	}

	@Override
	public InputStream getResourceAsStream(String name, @Nullable ClassLoader except) {
		return this.get(name, except, ChildClassLoader::searchStream, ClassLoader::getResourceAsStream);
	}

	@Override
	public ClassLoader instance() {
		return this;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> cls = this.findClass(name, resolve, null);
		if(cls != null) {
			return cls;
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	@Nullable
	@Override
	public URL getResource(String name) {
		return this.getResource(name, null);
	}

	@Override
	public Enumeration<URL> getResources(String name) {
		return this.getResources(name, null);
	}

	@Nullable
	@Override
	public InputStream getResourceAsStream(String name) {
		return this.getResourceAsStream(name, null);
	}

	@Nullable
	private <T> T get(String name, @Nullable ClassLoader except, BiFunction<ChildClassLoader, String, T> func, Fallback<T> fallback) {
		for(ClassLoader clsLdr : this.loaders) {
			T cls = func.apply((ChildClassLoader) clsLdr, name);
			if(clsLdr != except && cls != null) {
				return cls;
			}
		}

		try {
			return fallback.get(this.mainLoader, name);
		} catch(ClassNotFoundException e) {
			return null;
		} catch(IOException e) {
			throw Main.rethrow(e);
		}
	}

	interface Fallback<T> {
		T get(ModClassLoader loader, String name) throws ClassNotFoundException, IOException;
	}

	public static class DynURLClassLoader extends URLClassLoader {
		static {
			registerAsParallelCapable();
		}

		public DynURLClassLoader(URL[] urls) {
			super(urls, NullClassLoader.INSTANCE);
		}

		@Override
		public void addURL(URL url) {
			super.addURL(url);
		}
	}
}
