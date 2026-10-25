package ru.pawjob.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Holds every module, ticks the enabled ones and watches their keybinds.
 */
public class ModuleManager {
	private final List<Module> modules = new ArrayList<>();
	private final Set<Module> heldKeys = new HashSet<>();

	public void init() {
		// Register your modules here:
		//
		// register(new MyModule());
	}

	public void register(Module module) {
		if (modules.contains(module)) {
			return;
		}

		modules.add(module);
	}

	public List<Module> getModules() {
		return modules;
	}

	public List<Module> getModules(Category category) {
		List<Module> result = new ArrayList<>();

		for (Module module : modules) {
			if (module.getCategory() == category) {
				result.add(module);
			}
		}

		return result;
	}

	public Module getModule(String name) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(name)) {
				return module;
			}
		}

		return null;
	}

	public void onTick() {
		MinecraftClient client = MinecraftClient.getInstance();

		// Keys must not fire while a screen is open or while a rebind is in progress.
		boolean keysEnabled = client.currentScreen == null && client.getWindow() != null;
		long handle = keysEnabled ? client.getWindow().getHandle() : 0L;

		for (Module module : modules) {
			if (keysEnabled && module.getKey() > GLFW.GLFW_KEY_UNKNOWN && !module.isListeningForBind()) {
				boolean down = InputUtil.isKeyPressed(handle, module.getKey());

				if (down && heldKeys.add(module)) {
					module.toggle();
				} else if (!down) {
					heldKeys.remove(module);
				}
			} else {
				heldKeys.remove(module);
			}

			if (module.isEnabled()) {
				module.onTick();
			}
		}
	}
}
