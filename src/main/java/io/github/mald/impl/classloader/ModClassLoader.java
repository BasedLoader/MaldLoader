package io.github.mald.impl.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import io.github.mald.impl.util.BiEnumeration;
import io.github.mald.v0.api.classloader.ExtendedClassLoader;
import io.github.mald.v0.api.transformer.Buf;
import io.github.mald.v0.api.transformer.BufferTransformer;
import io.github.mald.v0.api.transformer.LazyDefiner;
import org.jetbrains.annotations.Nullable;

public class ModClassLoader extends ExtendedClassLoader.Secure {
	static {
		registerAsParallelCapable();
	}

	final ClassLoader mods;
	BufferTransformer transformer = Buf::new;
	LazyDefiner preParent = name -> null, postParent = name -> null;
	byte[] readBuffer = new byte[8196];

	public ModClassLoader(ClassLoader parent, ClassLoader mods) {
		super(parent);
		this.mods = mods;
	}

	public ModClassLoader(ClassLoader mods) {
		this.mods = mods;
	}

	@Override
	public boolean isClassLoaded(String name) {
		synchronized(this.getClassLoadingLock(name)) {
			return this.findLoadedClass(name) != null;
		}
	}

	public ModClassLoader setTransformer(BufferTransformer transformer) {
		this.transformer = transformer;
		return this;
	}

	public ModClassLoader setPreParent(LazyDefiner preParent) {
		this.preParent = preParent;
		return this;
	}

	public ModClassLoader setPostParent(LazyDefiner postParent) {
		this.postParent = postParent;
		return this;
	}

	public Class<?> define(String name, byte[] buf, int off, int len) {
		return this.defineClass(name, buf, off, len);
	}

	@Override
	public Class<?> findClass(String name) {
		Buf buf = this.postParent.forName(name);
		if(buf != null) {
			buf = this.transformer.transform(buf.code, buf.off, buf.len);
			if(buf != null) {
				return this.defineClass(name, buf.code, buf.off, buf.len);
			}
		}
		return null;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized(this.getClassLoadingLock(name)) {
			Class<?> c = this.findLoadedClass(name);
			if(c != null) {
				return c;
			}
			InputStream stream = this.mods.getResourceAsStream(name.replace('.', '/') + ".class");
			if(stream != null) {
				try {
					int len = this.readAll(stream, this.readBuffer);
					Buf buf = this.transformer.transform(this.readBuffer, 0, len);
					if(buf != null) {
						c = this.defineClass(name, buf.code, buf.off, buf.len);
					}
				} catch(IOException e) {
					throw Main.rethrow(e);
				}
			}

			if(c == null) {
				Buf buf = this.preParent.forName(name);
				if(buf != null) {
					Buf tr = this.transformer.transform(buf.code, buf.off, buf.len);
					if(tr != null) {
						c = this.defineClass(name, tr.code, tr.off, tr.len);
					}
				}
			}
			if(c != null && resolve) {
				this.resolveClass(c);
			}
		}
		return super.loadClass(name, resolve);
	}

	@Nullable
	@Override
	public URL getResource(String name) {
		URL stream = this.mods.getResource(name);
		if(stream != null) {
			return stream;
		} else {
			return super.getResource(name);
		}
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		Enumeration<URL> resource = this.mods.getResources(name);
		if(resource.hasMoreElements()) {
			return new BiEnumeration<>(resource, () -> {
				try {
					return super.getResources(name);
				} catch(IOException e) {
					throw Main.rethrow(e);
				}
			});
		} else {
			return super.getResources(name);
		}
	}

	@Nullable
	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream stream = this.mods.getResourceAsStream(name);
		if(stream != null) {
			return stream;
		} else {
			return super.getResourceAsStream(name);
		}
	}

	int readAll(InputStream stream, byte[] buf) throws IOException {
		int offset = 0, read;
		while((read = stream.read(buf)) != -1) {
			offset += read;
			if(offset >= buf.length) {
				this.readBuffer = buf = Arrays.copyOf(buf, buf.length << 1);
			}
		}
		return offset;
	}
}
