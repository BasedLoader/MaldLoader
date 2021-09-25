package com.maldloader.impl.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Originally from https://github.com/FabricMC/fabric-loader/blob/44c957c685bd0bc0160553381e134ac2775330e0/src/main/java/net/fabricmc/loader/impl
 * /launch/knot/KnotClassDelegate.java#L87
 * copied basically verbatim
 */
public class ProtectionDomainFinder {
	private final Map<String, Metadata> metadataCache = new HashMap<>();
	public Metadata getMetadata(String name, URL resourceURL) {
		if(resourceURL != null) {
			URL codeSourceURL = null;
			String filename = name.replace('.', '/') + ".class";

			try {
				codeSourceURL = getSource(filename, resourceURL);
			} catch(RuntimeException e) {
				System.err.println("Could not find code source for " + resourceURL + ": " + e.getMessage());
			}

			if(codeSourceURL != null) {
				return this.metadataCache.computeIfAbsent(codeSourceURL.toString(), (codeSourceStr) -> {
					Manifest manifest = null;
					CodeSource codeSource = null;
					Certificate[] certificates = null;
					URL fCodeSourceUrl = null;

					try {
						fCodeSourceUrl = new URL(codeSourceStr);
						Path path = asPath(fCodeSourceUrl);

						if(Files.isRegularFile(path)) {
							URLConnection connection = new URL("jar:" + codeSourceStr + "!/").openConnection();

							if(connection instanceof JarURLConnection) {
								manifest = ((JarURLConnection) connection).getManifest();
								certificates = ((JarURLConnection) connection).getCertificates();
							}

							if(manifest == null) {
								try(FileSystemDelegate jarFs = getJarFileSystem(path, false)) {
									Path manifestPath = jarFs.get().getPath("META-INF/MANIFEST.MF");

									if(Files.exists(manifestPath)) {
										try(InputStream stream = Files.newInputStream(manifestPath)) {
											manifest = new Manifest(stream);

											// TODO
											/* JarEntry codeEntry = codeSourceJar.getJarEntry(filename);
											if (codeEntry != null) {
												codeSource = new CodeSource(codeSourceURL, codeEntry.getCodeSigners());
											} */
										}
									}
								}
							}
						}
					} catch(IOException | RuntimeException e) {
						if(Boolean.getBoolean("mald.dev")) {
							System.err.println("Failed to load manifest: " + e);
							e.printStackTrace();
						}
					}

					if(codeSource == null) {
						codeSource = new CodeSource(fCodeSourceUrl, certificates);
					}

					return new Metadata(manifest, codeSource);
				});
			}
		}

		return Metadata.EMPTY;
	}

	public static URL getSource(String filename, URL resourceURL) {
		URL codeSourceURL;

		try {
			URLConnection connection = resourceURL.openConnection();
			if (connection instanceof JarURLConnection) {
				codeSourceURL = ((JarURLConnection) connection).getJarFileURL();
			} else {
				String path = resourceURL.getPath();

				if (path.endsWith(filename)) {
					codeSourceURL = new URL(resourceURL.getProtocol(), resourceURL.getHost(), resourceURL.getPort(), path.substring(0, path.length() - filename.length()));
				} else {
					throw new RuntimeException("Could not figure out code source for file '" + filename + "' and URL '" + resourceURL + "'!");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return codeSourceURL;
	}

	public static File asFile(URL url) {
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static Path asPath(URL url) {
		if (url.getProtocol().equals("file")) {
			// TODO: Is this required?
			return asFile(url).toPath();
		} else {
			try {
				return Paths.get(url.toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class Metadata {
		static final Metadata EMPTY = new Metadata(null, null);

		public final Manifest manifest;
		public final CodeSource codeSource;

		Metadata(Manifest manifest, CodeSource codeSource) {
			this.manifest = manifest;
			this.codeSource = codeSource;
		}
	}

	public static class FileSystemDelegate implements AutoCloseable {
		private final FileSystem fileSystem;
		private final boolean owner;

		public FileSystemDelegate(FileSystem fileSystem, boolean owner) {
			this.fileSystem = fileSystem;
			this.owner = owner;
		}

		public FileSystem get() {
			return this.fileSystem;
		}

		@Override
		public void close() throws IOException {
			if (this.owner) {
				this.fileSystem.close();
			}
		}
	}

	private static final Map<String, String> jfsArgsCreate = new HashMap<>();
	private static final Map<String, String> jfsArgsEmpty = new HashMap<>();

	static {
		jfsArgsCreate.put("create", "true");
	}

	public static FileSystemDelegate getJarFileSystem(Path path, boolean create) throws IOException {
		return getJarFileSystem(path.toUri(), create);
	}

	public static FileSystemDelegate getJarFileSystem(URI uri, boolean create) throws IOException {
		URI jarUri;

		try {
			jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		try {
			return new FileSystemDelegate(FileSystems.newFileSystem(jarUri, create ? jfsArgsCreate : jfsArgsEmpty), true);
		} catch (FileSystemAlreadyExistsException e) {
			return new FileSystemDelegate(FileSystems.getFileSystem(jarUri), false);
		}
	}
}
