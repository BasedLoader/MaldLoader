package io.github.mald.impl.classloader;

import java.util.ArrayList;
import java.util.List;

import io.github.mald.v0.api.transformer.Buf;
import io.github.mald.v0.api.transformer.BufferTransformer;
import org.jetbrains.annotations.Nullable;

public class MultiBufferTransformer implements BufferTransformer {
	final List<BufferTransformer> transformers = new ArrayList<>();

	@Override
	public @Nullable Buf transform(byte[] code, int off, int len) {
		Buf buf = null;
		for(BufferTransformer transformer : transformers) {
			buf = transformer.transform(code, off, len);
			if(buf == null) return null;
			code = buf.code;
			off = buf.off;
			len = buf.len;
		}
		return buf == null ?  new Buf(code, off, len) : buf;
	}
}
