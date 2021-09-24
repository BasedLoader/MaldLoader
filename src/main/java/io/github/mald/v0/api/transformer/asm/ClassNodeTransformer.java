package io.github.mald.v0.api.transformer.asm;

import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeTransformer {
	void accept(ClassNode visitor);

	/**
	 * @return whether the transformer will transform the given class
	 */
	boolean transforms(ClassHeader header);
}
