package io.github.mald.v0.api.transformer;

public class Buf {
	public final byte[] code;
	public final int off;
	public final int len;

	public Buf(byte[] code, int off, int len) {
		this.code = code;
		this.off = off;
		this.len = len;
	}

	public Buf(byte[] code) {
		this(code, 0, code.length);
	}
}
