package io.github.mald.impl.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

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
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public void prepare() {

	}

	@Override
	public MixinEnvironment.Phase getInitialPhase() {
		return null;
	}

	@Override
	public void offer(IMixinInternal internal) {

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
		return null;
	}

	@Override
	public IClassProvider getClassProvider() {
		return null;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return null;
	}

	@Override
	public ITransformerProvider getTransformerProvider() {
		return null;
	}

	@Override
	public IClassTracker getClassTracker() {
		return null;
	}

	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}

	@Override
	public Collection<String> getPlatformAgents() {
		return null;
	}

	@Override
	public IContainerHandle getPrimaryContainer() {
		return null;
	}

	@Override
	public Collection<IContainerHandle> getMixinContainers() {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return null;
	}

	@Override
	public String getSideName() {
		return null;
	}

	@Override
	public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
		return null;
	}

	@Override
	public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
		return null;
	}

	@Override
	public ILogger getLogger(String name) {
		return null;
	}

	@Override
	public Collection<ITransformer> getTransformers() {
		return null;
	}

	@Override
	public Collection<ITransformer> getDelegatedTransformers() {
		return null;
	}

	@Override
	public void addTransformerExclusion(String name) {

	}

	public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		throw new RuntimeException("Not Implemented");
	}
}
