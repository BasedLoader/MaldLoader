package com.maldloader.testmod.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.main.Main;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	protected TitleScreenMixin(Component $$0) {
		super($$0);
	}

	@Inject(method = "createNormalMenuOptions", at = @At("TAIL"))
	private void shiftAndAddButtons(int $$0, int $$1, CallbackInfo ci) {

	}

	@Override
	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
		if(widget instanceof Button button) {
			System.out.println(button.getMessage().getString());
			throw new RuntimeException("No4");
		} else {
			throw new RuntimeException("No");
		}
//		return super.addRenderableWidget(widget);
	}
}
