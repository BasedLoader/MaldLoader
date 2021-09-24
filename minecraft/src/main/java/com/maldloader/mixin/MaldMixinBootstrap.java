package com.maldloader.mixin;

import com.maldloader.v0.api.modloader.ModMetadata;
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

    public static <T extends MixinModMetadata> void loadMixinMods(Collection<T> mods) {
		MixinService.boot();
        MixinBootstrap.init();
        for (MixinModMetadata mod : mods) {
            if(!mod.mixinFiles().isEmpty()) {
	            for(String file : mod.mixinFiles()) {
		            Mixins.addConfiguration(file);
	            }
            }
        }
    }

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
		// TODO: see MixinServiceLaunchWrapperBootstrap
	}
}
