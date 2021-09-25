package com.maldloader.mixin;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IMixinServiceBootstrap;
import org.spongepowered.asm.service.MixinService;

import java.util.Collection;
import java.util.List;

/**
 * Allows mixin to start
 */
public class MaldMixinBootstrap implements IMixinServiceBootstrap {

	@Override
	public String getName() {
		return "MaldLoader";
	}

	@Override
	public String getServiceClassName() {
		return "com.maldloader.mixin";
	}

	@Override
	public void bootstrap() {
	}
}
