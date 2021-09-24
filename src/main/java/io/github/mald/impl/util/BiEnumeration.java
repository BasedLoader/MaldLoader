package io.github.mald.impl.util;

import java.util.Enumeration;
import java.util.function.Supplier;

public class BiEnumeration<T> implements Enumeration<T> {
	public Enumeration<T> enumeration;
	public Supplier<Enumeration<T>> next;

	public BiEnumeration(Enumeration<T> enumeration, Supplier<Enumeration<T>> next) {
		this.enumeration = enumeration;
		this.next = next;
	}

	@Override
	public boolean hasMoreElements() {
		Enumeration<T> e = this.enumeration;
		if(e.hasMoreElements()) {
			return true;
		} else if(this.next != null) {
			this.enumeration = this.next.get();
			this.next = null;
			return this.enumeration.hasMoreElements();
		} else {
			return false;
		}
	}

	@Override
	public T nextElement() {
		Enumeration<T> e = this.enumeration;
		if(e.hasMoreElements()) {
			return e.nextElement();
		} else if(this.next != null) {
			this.enumeration = this.next.get();
			this.next = null;
			return this.enumeration.nextElement();
		} else {
			throw new IllegalStateException("crab");
		}
	}
}
