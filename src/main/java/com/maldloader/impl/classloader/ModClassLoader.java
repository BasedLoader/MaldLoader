package com.maldloader.impl.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.maldloader.impl.util.BiEnumeration;
import com.maldloader.impl.util.ProtectionDomainFinder;
import com.maldloader.v0.api.NullClassLoader;
import com.maldloader.v0.api.classloader.DefaultChildClassLoader;
import com.maldloader.v0.api.classloader.ExtendedClassLoader;
import com.maldloader.v0.api.transformer.Buf;
import com.maldloader.v0.api.transformer.BufferTransformer;
import com.maldloader.v0.api.transformer.LazyDefiner;
import org.jetbrains.annotations.Nullable;

public class ModClassLoader extends ExtendedClassLoader.Secure implements DefaultChildClassLoader.Access {
	static {
		registerAsParallelCapable();
	}

	final ProtectionDomainFinder finder = new ProtectionDomainFinder();
	final ClassLoader mods;
	BufferTransformer transformer = Buf::new;
	LazyDefiner preParent = name -> null, postParent = name -> null;
	byte[] readBuffer = new byte[8196];

	public ModClassLoader(ClassLoader parent, ClassLoader mods) {
		super(parent);
		this.mods = mods;
	}

	public ModClassLoader(ClassLoader mods) {
		this(NullClassLoader.INSTANCE, mods);
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
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized(this.getClassLoadingLock(name)) {
			Class<?> c = this.findLoadedClass(name);
			if(c != null) {
				return c;
			}
			String clsName = name.replace('.', '/') + ".class";
			URL resource = this.mods.getResource(clsName);
			if(resource != null) {
				try {
					InputStream stream = resource.openStream();
					int len = this.readAll(stream, this.readBuffer);
					Buf buf = this.transformer.transform(this.readBuffer, 0, len);
					if(buf != null) {
						ProtectionDomainFinder.Metadata metadata = this.finder.getMetadata(name, resource);
						c = this.defineClass(name, buf.code, buf.off, buf.len, metadata.codeSource);
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

			if(c != null) {
				int pkgDelimiterPos = name.lastIndexOf('.');
				if(pkgDelimiterPos > 0) {
					String pkgString = name.substring(0, pkgDelimiterPos);
					if(this.getPackage(pkgString) == null) {
						this.definePackage(pkgString, null, null, null, null, null, null, null);
					}
				}

				if(resolve) {
					this.resolveClass(c);
				}
			}

			if(c == null) {
				c = super.loadClass(name, resolve);
			}
			return c;
		}
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

	@Override
	public Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException {
		return this.loadClass(name, resolve);
	}

	int readAll(InputStream stream, byte[] buf) throws IOException {
		int offset = 0, read;
		while((read = stream.read(buf, offset, buf.length - offset)) != -1) {
			offset += read;
			if(offset >= buf.length) {
				this.readBuffer = buf = Arrays.copyOf(buf, buf.length << 1);
			}
		}
		return offset;
	}
}