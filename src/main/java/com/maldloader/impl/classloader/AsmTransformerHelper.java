package com.maldloader.impl.classloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.maldloader.v0.api.transformer.Buf;
import com.maldloader.v0.api.transformer.BufferTransformer;
import com.maldloader.v0.api.transformer.asm.ReaderFlagGetter;
import com.maldloader.v0.api.transformer.asm.ClassHeader;
import com.maldloader.v0.api.transformer.asm.ClassNodeTransformer;
import com.maldloader.v0.api.transformer.asm.ClassVisitorTransformer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class AsmTransformerHelper implements BufferTransformer {
	final List<ClassVisitorTransformer> visit = new ArrayList<>();
	final List<ClassNodeTransformer> node = new ArrayList<>();
	final Set<ReaderFlagGetter> flags = new HashSet<>();

	@Override
	public @Nullable Buf transform(byte[] code, int off, int len) {
		ClassReader reader = new ClassReader(code, off, len);
		String name = reader.getClassName(), superName = reader.getSuperName(), ifaces[] = reader.getInterfaces();
		List<String> interfaces = Collections.unmodifiableList(Arrays.asList(ifaces));
		ClassHeader header = new ClassHeader(reader.getAccess(), name, superName, interfaces);

		int writerFlags = 0;
		for(ReaderFlagGetter flags : this.flags) {
			writerFlags |= flags.getClassWriterFlags(header);
		}

		boolean requiresNode = false;
		for(ClassNodeTransformer transformer : this.node) {
			requiresNode |= transformer.transforms(header);
		}

		ClassWriter writer = new ClassWriter(writerFlags);
		ClassVisitor visitor = writer;
		for(ClassVisitorTransformer transformer : this.visit) {
			visitor = transformer.accept(header, visitor);
		}

		if(requiresNode) {
			ClassNode node = new ClassNode();
			reader.accept(node, 0);
			for(ClassNodeTransformer transformer : this.node) {
				transformer.accept(node);
			}
			node.accept(writer);
			return new Buf(writer.toByteArray());
		} else if(visitor != writer) {
			reader.accept(writer, 0);
			return new Buf(writer.toByteArray());
		} else {
			return new Buf(code, off, len);
		}
	}
}
