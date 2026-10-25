package ru.pawjob;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.pawjob.config.ConfigManager;
import ru.pawjob.gui.ClickGui;
import ru.pawjob.gui.Hud;
import ru.pawjob.module.ModuleManager;

public class PawjobClient implements ClientModInitializer {
	public static final String MOD_ID = "pawjob";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static PawjobClient instance;

	private final ModuleManager moduleManager = new ModuleManager();
	private final ConfigManager configManager = new ConfigManager();

	private KeyBinding openGuiKey;

	@Override
	public void onInitializeClient() {
		instance = this;

		moduleManager.init();
		configManager.load();

		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.pawjob.open_gui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.pawjob.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.wasPressed()) {
				client.setScreen(new ClickGui());
			}

			moduleManager.onTick();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.save());

		HudRenderCallback.EVENT.register((context, tickCounter) -> Hud.render(context));

		LOGGER.info("[pawjob] loaded with {} modules", moduleManager.getModules().size());
	}

	public static PawjobClient getInstance() {
		return instance;
	}

	public ModuleManager getModules() {
		return moduleManager;
	}

	public ConfigManager getConfig() {
		return configManager;
	}
}
