package io.github.mald.impl.mixin;

import io.github.mald.v0.api.modloader.ModMetadata;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IMixinServiceBootstrap;
import org.spongepowered.asm.service.MixinService;

import java.util.List;

/**
 * Allows mixin to start
 */
public class MaldMixinBootstrap implements IMixinServiceBootstrap {

    public static void loadMixinMods(List<ModMetadata> mods) {
		MixinService.boot();
        MixinBootstrap.init();
        for (ModMetadata mod : mods) {
            if(mod.mixinFile() != null)
                Mixins.addConfiguration(mod.mixinFile());
        }
    }

	@Override
	public String getName() {
		return "MaldLoader";
	}

	@Override
	public String getServiceClassName() {
		return "io.github.mald.impl.mixin";
	}

	@Override
	public void bootstrap() {
		// TODO: see MixinServiceLaunchWrapperBootstrap
	}
}
