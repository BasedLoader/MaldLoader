package io.github.mald.impl.classloader;

public class Buf {
	final byte[] code;
	final int off;
	final int len;

	public Buf(byte[] code, int off, int len) {
		this.code = code;
		this.off = off;
		this.len = len;
	}
}
