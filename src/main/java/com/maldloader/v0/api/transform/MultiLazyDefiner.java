package com.maldloader.v0.api.transform;

import java.util.ArrayList;
import java.util.List;

import com.maldloader.v0.api.transformer.Buf;
import com.maldloader.v0.api.transformer.LazyDefiner;
import org.jetbrains.annotations.Nullable;

public class MultiLazyDefiner implements LazyDefiner {
	final List<LazyDefiner> definers = new ArrayList<>();

	public MultiLazyDefiner add(LazyDefiner definer) {
		this.definers.add(definer);
		return this;
	}

	@Override
	public @Nullable Buf forName(String name) {
		for(LazyDefiner definer : this.definers) {
			Buf buf = definer.forName(name);
			if(buf != null) {
				return buf;
			}
		}
		return null;
	}
}
