package io.github.mald.impl.mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;
import org.spongepowered.asm.service.mojang.Blackboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class MaldGlobalPropertyService implements IGlobalPropertyService {

	//TODO: MaldLoader.getProperties instead of this
	public static final Map<String, Object> PROPERTIES = new HashMap<>();

	@Override
	public IPropertyKey resolveKey(String name) {
		return new StringPropertyKey(name);
	}

	@Override
	public <T> T getProperty(IPropertyKey key) {
		return (T) PROPERTIES.get(getString(key));
	}

	@Override
	public void setProperty(IPropertyKey key, Object value) {
		PROPERTIES.put(getString(key), value);
	}

	@Override
	public <T> T getProperty(IPropertyKey key, T defaultValue) {
		return (T) PROPERTIES.getOrDefault(getString(key), defaultValue);
	}

	@Override
	public String getPropertyString(IPropertyKey key, String defaultValue) {
		Object property = PROPERTIES.get(getString(key));
		return property != null ? property.toString() : defaultValue;
	}

	public String getString(IPropertyKey key) {
		return ((StringPropertyKey) key).key;
	}

	// if we decide to make java 1.16+ dependent, use a record?
	public static class StringPropertyKey implements IPropertyKey {

		public final String key;

		public StringPropertyKey(String key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			StringPropertyKey that = (StringPropertyKey) o;
			return Objects.equals(key, that.key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(key);
		}

		@Override
		public String toString() {
			return this.key;
		}
	}

	static {
//		PROPERTIES.put()
	}
}
