package ru.pawjob.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import ru.pawjob.PawjobClient;
import ru.pawjob.module.Module;

/**
 * Draws the array list of enabled modules in the top right corner.
 */
public final class Hud {
	private static final int ACCENT_COLOR = 0xFF55FF88;
	private static final int TEXT_COLOR = 0xFFD8D8D8;
	private static final int MARGIN = 4;

	private Hud() {
	}

	public static void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.options.hudHidden) {
			return;
		}

		List<Module> enabled = new ArrayList<>();

		for (Module module : PawjobClient.getInstance().getModules().getModules()) {
			if (module.isEnabled()) {
				enabled.add(module);
			}
		}

		enabled.sort(Comparator.comparing(Module::getName));

		int x = client.getWindow().getScaledWidth() - MARGIN;
		int y = MARGIN;

		String title = "pawjob";
		context.drawTextWithShadow(client.textRenderer, title, x - client.textRenderer.getWidth(title), y, ACCENT_COLOR);
		y += 11;

		for (Module module : enabled) {
			String name = module.getName();
			context.drawTextWithShadow(client.textRenderer, name, x - client.textRenderer.getWidth(name), y, TEXT_COLOR);
			y += 10;
		}
	}
}
