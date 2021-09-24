package io.github.mald.impl.classloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.mald.impl.LoaderPluginLoader;
import io.github.mald.v0.api.LoaderList;
import io.github.mald.v0.api.modloader.ModFiles;
import io.github.mald.v0.api.modloader.ModLoader;
import io.github.mald.v0.api.plugin.LoaderPlugin;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Main {
	public static List<ModFiles> getPathsViaProperty(String namespace, String defaultDirectory) {
		try {
			return getPathsViaProperty0(namespace, defaultDirectory);
		} catch(IOException e) {
			throw rethrow(e);
		}
	}

	// todo make this resolve mod files rather than path
	public static List<ModFiles> getPathsViaProperty0(String namespace, String defaultDirectory) throws IOException {
		String path = System.clearProperty(namespace + ".modDir");
		if(path == null) {
			path = defaultDirectory;
		}
		List<ModFiles> loaderPlugins = new ArrayList<>();
		if(path != null) {
			File dir = new File(path);
			if(dir.exists() && dir.isDirectory()) {
				for(File file : Objects.requireNonNull(dir.listFiles())) {
					loaderPlugins.add(ModFiles.autoDetect(file.toPath()));
				}
			}
		}

		String plugins = System.clearProperty(namespace + ".mods");
		if(plugins != null) {
			for(String s : plugins.split(",")) {
				loaderPlugins.add(ModFiles.autoDetect(Paths.get(s)));
			}
		}

		String lsv = System.clearProperty(namespace + ".modlist");
		if(lsv != null) {
			try(BufferedReader reader = Files.newBufferedReader(Paths.get(lsv))) {
				String[] split = reader.readLine().split(" ");
				Path[] paths = new Path[split.length];
				for(int i = 0; i < split.length; i++) {
					paths[i] = Paths.get(split[i]);
				}
				if(paths.length > 1) {
					loaderPlugins.add(ModFiles.directory(paths));
				} else {
					loaderPlugins.add(ModFiles.autoDetect(paths[0]));
				}
			} catch(IOException e) {
				throw rethrow(e);
			}
		}

		return loaderPlugins;
	}

	public static void main(String[] args) throws Throwable {
		List<ModFiles> loaderPlugins = getPathsViaProperty(LoaderPluginLoader.MALD, LoaderPluginLoader.MALD + "_plugins");
		launch(loaderPlugins, args);
	}

	public static void launch(List<ModFiles> loaderPlugins, String[] args) throws Throwable {
		MainClassLoaderImpl[] main = {null};
		Method method = loadFromFile(loaderPlugins, main);

		// a big hack to minimize the stacktrace size
		ClassWriter writer = new ClassWriter(0);
		String[] interfaces = new String[] {"java/util/function/Consumer"};
		writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, LoaderPluginLoader.MALD + "/Launcher_Generated", null, "java/lang/Object", interfaces);
		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		visitor.visitVarInsn(Opcodes.ALOAD, 0);
		visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		visitor.visitInsn(Opcodes.RETURN);
		visitor.visitMaxs(1, 1);
		visitor.visitEnd();

		MethodVisitor run = writer.visitMethod(Opcodes.ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null);
		run.visitVarInsn(Opcodes.ALOAD, 1);
		run.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/String;");
		run.visitMethodInsn(Opcodes.INVOKESTATIC,
				Type.getInternalName(method.getDeclaringClass()),
				method.getName(),
				Type.getMethodDescriptor(method),
				false);
		run.visitInsn(Opcodes.RETURN);
		run.visitMaxs(1, 2);
		run.visitEnd();

		byte code[] = writer.toByteArray();
		String name = LoaderPluginLoader.MALD + ".Launcher_Generated";
		Class<?> type = main[0].define(name, code, 0, code.length);
		Thread thread = Thread.currentThread();
		thread.setContextClassLoader(main[0]);
		Consumer<String[]> consumer = (Consumer<String[]>) type.newInstance();
		consumer.accept(args);
	}

	public static Method loadFromFile(List<ModFiles> loaderPlugins, MainClassLoaderImpl[] ref) throws Throwable {
		LoaderPluginLoader impl = new LoaderPluginLoader(loaderPlugins);
		ModClassLoader[] ref1 = {null};
		Map<String, LoaderPlugin> plugins = impl.init(Main.class.getClassLoader(), ref1);
		try {
			List<ModLoader<?>> loaders = new ArrayList<>();
			for(LoaderPlugin plugin : plugins.values()) {
				plugin.offerModLoaders(loaders::add);
			}

			MainClassLoaderImpl main = new MainClassLoaderImpl(ref1[0]);
			LoaderList mald = new LoaderList(plugins, loaders);

			for(ModLoader<?> loader : loaders) {
				loader.init(mald, main);
			}

			for(LoaderPlugin value : plugins.values()) {
				value.afterModLoaderInit(mald, main);
			}

			Map<String, Class<?>> mainClasses = new HashMap<>();
			for(LoaderPlugin plugin : plugins.values()) {
				plugin.offerMainClasses(mald, main, (id, cls) -> {
					if(!mainClasses.containsKey(id)) {
						mainClasses.put(id, cls);
					} else {
						System.err.println("Multiple main classes for " + cls);
					}
				});
			}

			String property = System.getProperty(LoaderPluginLoader.MALD + ".main");
			if(property == null) {
				throw new IllegalStateException("no main class property set! " + mainClasses.keySet());
			}
			Class<?> cls = mainClasses.get(property);
			if(cls == null) {
				throw new IllegalStateException("no main class for '" + property + "'");
			}
			ref[0] = main;
			return cls.getDeclaredMethod("main", String[].class);
		} finally {
			for(LoaderPlugin value : plugins.values()) {
				if(value instanceof AutoCloseable) {
					try {
						((AutoCloseable) value).close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @return nothing, because it throws
	 * @throws T rethrows {@code throwable}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
		throw (T) throwable;
	}
}
