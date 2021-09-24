package io.github.mald.impl.classloader;

import org.jetbrains.annotations.Nullable;

public interface BufferTransformer {

	/**
	 * transform the buffer, the returned array, length, andor offset may be the same
	 */
	@Nullable
	Buf transform(byte[] code, int off, int len);
}
