package com.maldloader.testmod.mixins;

import net.minecraft.client.main.Main;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class TitleScreenMixin {
	@Inject(method = "main", at = @At("HEAD"))
	private static void testMixin(CallbackInfo ci) {
		for(int i = 0; i < 20; i++) {
			System.out.println("Please Continue to mald, seethe and cope");
		}
	}
}
