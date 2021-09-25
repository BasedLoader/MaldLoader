package com.maldloader.testmod.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class ModsScreen extends Screen {

	private final Screen previousWindow;

	public ModsScreen(Screen previousWindow) {
		super(new TranslatableComponent("screen.mods"));
		this.previousWindow = previousWindow;
	}

	@Override
	public void render(PoseStack stack, int $$1, int $$2, float $$3) {
		this.renderBackground(stack);
		super.render(stack, $$1, $$2, $$3);
	}
}
