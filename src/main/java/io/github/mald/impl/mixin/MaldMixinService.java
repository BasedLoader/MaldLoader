package io.github.mald.impl.mixin;

import io.github.mald.v0.api.modloader.ModLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public class MaldMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {

	static IMixinTransformer transformer;

	private final ReEntranceLock lock;

	public MaldMixinService() {
		lock = new ReEntranceLock(1);
	}

	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
		return getClassNode(name, true);
	}

	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		ClassReader reader = new ClassReader(getClassBytes(name, runTransformers));
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		return node;
	}

	@Override
	public URL[] getClassPath() {
		return new URL[0];
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return null;
	}

	@Override
	public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
		return null;
	}

	@Override
	public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
		return null;
	}

	@Override
	public void registerInvalidClass(String className) {

	}

	@Override
	public boolean isClassLoaded(String className) {
		return false;
	}

	@Override
	public String getClassRestrictions(String className) {
		return null;
	}

	@Override
	public String getName() {
		return "Mald/MaldLoader";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void prepare() {
	}

	@Override
	public MixinEnvironment.Phase getInitialPhase() {
		return MixinEnvironment.Phase.PREINIT;
	}

	@Override
	public void offer(IMixinInternal internal) {
		if (internal instanceof IMixinTransformerFactory) {
			transformer = ((IMixinTransformerFactory) internal).createTransformer();
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void beginPhase() {
	}

	@Override
	public void checkEnv(Object bootSource) {
	}

	@Override
	public ReEntranceLock getReEntranceLock() {
		return lock;
	}

	@Override
	public IClassProvider getClassProvider() {
		return this;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return this;
	}

	@Override
	public ITransformerProvider getTransformerProvider() {
		return this;
	}

	@Override
	public IClassTracker getClassTracker() {
		return this;
	}

	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}

	@Override
	public Collection<String> getPlatformAgents() {
		return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
	}

	@Override
	public IContainerHandle getPrimaryContainer() {
		try {
			return new ContainerHandleURI(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<IContainerHandle> getMixinContainers() {
		return Collections.emptyList();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return this.getClass().getClassLoader().getResourceAsStream(name); //TODO: this probably isnt a good idea
	}

	@Override
	public String getSideName() {
		return "CLIENT"; // TODO: wait for access to the side
	}

	@Override
	public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
		return MixinEnvironment.CompatibilityLevel.JAVA_8;
	}

	@Override
	public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
		return MixinEnvironment.CompatibilityLevel.JAVA_18;
	}

	@Override
	public ILogger getLogger(String name) { //TODO: better logger
		return new LoggerAdapterAbstract(name) {
			@Override
			public String getType() {
				return "Mald Loader Mixin Logger";
			}

			@Override
			public void catching(Level level, Throwable t) {
				log(Level.ERROR, "Caught ".concat(t.toString()), t);
			}

			@Override
			public void log(Level level, String message, Object... params) {
				String formattedMessage = String.format(message, params);
				switch (level) {
					case INFO, WARN -> System.out.println(formattedMessage);
					case ERROR, FATAL, TRACE -> System.err.println(formattedMessage);
					default -> {
					}
				}
			}

			@Override
			public void log(Level level, String message, Throwable t) {
				log(level, message);
				t.printStackTrace();
			}

			@Override
			public <T extends Throwable> T throwing(T t) {
				log(Level.ERROR, "Throwing ".concat(t.toString()), t);
				return t;
			}
		};
	}

	@Override
	public Collection<ITransformer> getTransformers() {
		return Collections.emptyList();
	}

	@Override
	public Collection<ITransformer> getDelegatedTransformers() {
		return Collections.emptyList();
	}

	@Override
	public void addTransformerExclusion(String name) {
	}

	public byte[] getClassBytes(String rawName, boolean runTransformers) throws IOException {
		String name = rawName.replace('.', '/') + ".class";
		try (InputStream iStream = this.getClass().getClassLoader().getResourceAsStream(name)) {
			return iStream.readAllBytes();
		}
	}
}
