package com.maldloader.impl.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.maldloader.v0.api.classloader.DynURLClassLoader;
import com.maldloader.v0.api.classloader.ExtendedClassLoader;
import com.maldloader.v0.api.classloader.MainClassLoader;
import com.maldloader.v0.api.classloader.ModClassLoader;
import com.maldloader.v0.api.transform.AsmTransformerHelper;
import com.maldloader.v0.api.transform.MultiBufferTransformer;
import com.maldloader.v0.api.transform.MultiLazyDefiner;
import com.maldloader.v0.api.transformer.BufferTransformer;
import com.maldloader.v0.api.transformer.LazyDefiner;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import com.maldloader.v0.api.transformer.asm.ClassVisitorTransformer;
import com.maldloader.v0.api.transformer.asm.WriterFlagGetter;
import org.jetbrains.annotations.Nullable;

public class MainClassLoaderImpl extends SecureClassLoader implements MainClassLoader {
	public static MainClassLoaderImpl instance;

	static {
		registerAsParallelCapable();
	}

	final ModClassLoader defaultLoader;
	final DynURLClassLoader modJarContainer;
	final List<DefaultChildClassLoader> loaders = new ArrayList<>();
	final Set<String> lock = Collections.newSetFromMap(new ConcurrentHashMap<>());
	final MultiBufferTransformer transformer = new MultiBufferTransformer();
	final AsmTransformerHelper helper = new AsmTransformerHelper();
	final MultiLazyDefiner pre = new MultiLazyDefiner(), post = new MultiLazyDefiner();

	public MainClassLoaderImpl(ModClassLoader loaderPlugins, boolean displace) {
		super(loaderPlugins);
		this.modJarContainer = new DynURLClassLoader(new URL[0]);
		this.defaultLoader = this.offerWrapped(this.modJarContainer);
		if(displace) {
			if(instance == null) {
				instance = this;
			} else {
				throw new UnsupportedOperationException("Cannot displace existing MainClassLoaderImpl!");
			}
		}
	}

	@Override
	public boolean isClassLoaded(String name) {
		return this.isClassLoaded(name, null);
	}

	@Override
	public Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException {
		return this.loadClass(name, resolve);
	}

	@Override
	public void addPreParentDefiner(LazyDefiner definer) {
		this.pre.add(definer);
	}

	@Override
	public void addPostParentDefiner(LazyDefiner definer) {
		this.post.add(definer);
	}

	@Override
	public void addTransformer(BufferTransformer transformer) {
		this.transformer.add(transformer);
	}

	@Override
	public void addVisitorTransformer(ClassVisitorTransformer transformer) {
		this.helper.addVisitorTransformer(transformer);
	}

	@Override
	public void addClassNodeTransformer(ClassNodeTransformer transformer) {
		this.helper.addClassNodeTransformer(transformer);
	}

	@Override
	public void addWriterFlags(WriterFlagGetter getter) {
		this.helper.addFlagTransformer(getter);
	}

	@Override
	public Class<?> define(String name, byte[] buf, int off, int len) {
		return this.defaultLoader.define(name, buf, off, len);
	}

	@Override
	public void offer(URL url) {
		this.modJarContainer.addURL(url);
	}

	@Override
	public ModClassLoader offer(Function<ClassLoader, ModClassLoader> loader) {
		DefaultChildClassLoader classLoader = new DefaultChildClassLoader(this);
		ModClassLoader created = loader.apply(classLoader);
		classLoader.set(created);
		created.getTransformer().add(this.transformer);
		created.getPreParent().add(this.pre);
		created.getPostParent().add(this.post);
		this.loaders.add(classLoader);
		return created;
	}

	@Override
	public boolean isClassLoaded(String name, @Nullable ClassLoader except) {
		return Boolean.TRUE.equals(this.get(name, except, ExtendedClassLoader::isClassLoaded, ModClassLoader::isClassLoaded));
	}

	@Override
	public @Nullable Class<?> findClass(String name, boolean resolve, @Nullable ClassLoader except) {
		Class<?> cls;
		if(this.lock.add(name)) {
			synchronized(this.getClassLoadingLock(name)) {
				cls = this.get(name, except, (c, n) -> c.searchClass(n, resolve), (c, n) -> c.loadClass(n, resolve));
			}
		} else {
			return null;
		}
		this.lock.remove(name);
		return cls;
	}

	@Override
	public URL getResource(String name, @Nullable ClassLoader except) {
		return this.get(name, except, DefaultChildClassLoader::searchResource, ModClassLoader::getResource);
	}

	@Override
	public Enumeration<URL> getResources(String name, @Nullable ClassLoader except) {
		Enumeration<URL> enumeration = this.get(name, except, DefaultChildClassLoader::searchResources, ModClassLoader::getResources);
		return enumeration == null ? Collections.emptyEnumeration() : enumeration;
	}

	@Override
	public InputStream getResourceAsStream(String name, @Nullable ClassLoader except) {
		return this.get(name, except, DefaultChildClassLoader::searchStream, ModClassLoader::getResourceAsStream);
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
	private <T> T get(String name, @Nullable ClassLoader except, BiFunction<DefaultChildClassLoader, String, T> func, Fallback<T> fallback) {
		for(DefaultChildClassLoader clsLdr : this.loaders) {
			if(clsLdr != except) {
				T val = func.apply(clsLdr, name);
				if(val instanceof Enumeration) {
					if(((Enumeration)val).hasMoreElements()) {
						return val;
					}
				} else if(val != null) {
					return val;
				}
			}
		}

		try {
			return fallback.get((ModClassLoader) this.getParent(), name);
		} catch(ClassNotFoundException e) {
			return null;
		} catch(IOException e) {
			throw Main.rethrow(e);
		}
	}

	interface Fallback<T> {
		T get(ModClassLoader loader, String name) throws ClassNotFoundException, IOException;
	}

}
