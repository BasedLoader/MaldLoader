package com.maldloader.v0.api.transformer.asm;

import java.util.List;

public class ClassHeader {
	public final int access;
	public final String internalName;
	public final String superName;
	public final List<String> interfaces;

	public ClassHeader(int access, String internalName, String superName, List<String> interfaces) {
		this.access = access;
		this.internalName = internalName;
		this.superName = superName;
		this.interfaces = interfaces;
	}
}
