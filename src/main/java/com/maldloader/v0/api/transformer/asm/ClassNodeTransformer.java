package com.maldloader.v0.api.transformer.asm;

import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeTransformer {
	void accept(ClassNode node);

	/**
	 * @return whether the transformer will transform the given class
	 */
	boolean transforms(ClassHeader header);
}
