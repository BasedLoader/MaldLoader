package com.maldloader.testmod.mixins;

import com.maldloader.testmod.screens.ModsScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	@Shadow
	@Final
	private boolean fading;

	@Shadow
	private long fadeInStart;

	protected TitleScreenMixin(Component $$0) {
		super($$0);
	}

	@Inject(method = "createNormalMenuOptions", at = @At("TAIL"))
	private void addModsButton(int $$0, int $$1, CallbackInfo ci) {
		int buttonStart = this.height / 4 + 48;
		this.addRenderableWidget(new Button(this.width / 2 - 100, buttonStart + 24 * 2, 98, 20,
				new TranslatableComponent("menu.mods"), button -> minecraft.setScreen(new ModsScreen(this))));
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isProbablyModded()Z", shift = At.Shift.AFTER))
	private void renderMaldInfo(PoseStack stack, int mouseX, int mouseY, float $$3, CallbackInfo ci) {
		drawString(stack, this.font, getModsText(), 2, this.height - 20, 16777215 | getFadeColour());
	}

	@Override
	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
		if (widget instanceof Button button) {
			if (button.getMessage() instanceof TranslatableComponent component) {
				// Find the vanilla realms button and modify its pos and width.
				if (component.getKey().equals("menu.online") && button.getWidth() == 200) {
					button.x = this.width / 2 + 2;
					button.setWidth(98);
				}
			}
		}
		return super.addRenderableWidget(widget);
	}

	private String getModsText() {
		int modCount = 1; //TODO: get it from the loader
		String modText = modCount == 1 ? "Mod" : "Mods";
		return modCount + " " + modText + " Loaded";
	}

	/**
	 * Small utility to calculate the fade color.
	 *
	 * @return the current color the TitleScreen is fading into
	 */
	private int getFadeColour() {
		float fadeAmount = this.fading ? (float) (Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
		float colorClamp = this.fading ? Mth.clamp(fadeAmount - 1.0F, 0.0F, 1.0F) : 1.0F;
		return Mth.ceil(colorClamp * 255.0F) << 24;
	}
}
