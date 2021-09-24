package io.github.mald.impl.mixin;

import io.github.mald.v0.api.modloader.ModMetadata;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.List;

/**
 * Allows mixin to start
 */
public class MaldMixinBootstrap {

    public static void loadMixinMods(List<ModMetadata> mods) {
        MixinBootstrap.init();
        for (ModMetadata mod : mods) {
            if(mod.mixinFile() != null)
                Mixins.addConfiguration(mod.mixinFile());
        }
    }
}
