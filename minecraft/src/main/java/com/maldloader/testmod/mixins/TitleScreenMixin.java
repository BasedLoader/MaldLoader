package com.maldloader.testmod.mixins;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void testMixin(CallbackInfo ci) {
		System.out.println("Please Continue to mald, seethe and cope");
	}
}
