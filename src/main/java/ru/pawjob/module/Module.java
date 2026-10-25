package ru.pawjob.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import ru.pawjob.module.setting.BooleanSetting;
import ru.pawjob.module.setting.NumberSetting;
import ru.pawjob.module.setting.Setting;

/**
 * Base class for everything the client can switch on and off.
 * A module belongs to a {@link Category}, can carry a keybind, owns a list of
 * {@link Setting}s, and is ticked every client tick while it is enabled.
 */
public abstract class Module {
	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();

	private final int defaultKey;
	private int key;
	private boolean enabled;
	private boolean listeningForBind;

	protected Module(String name, String description, Category category) {
		this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
	}

	protected Module(String name, String description, Category category, int defaultKey) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.defaultKey = defaultKey;
		this.key = defaultKey;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	public String getId() {
		return name.toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	public int getDefaultKey() {
		return defaultKey;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;

		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	/** True while the ClickGUI is waiting for a key press to rebind this module. */
	public boolean isListeningForBind() {
		return listeningForBind;
	}

	public void setListeningForBind(boolean listeningForBind) {
		this.listeningForBind = listeningForBind;
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	/** Called once per client tick while the module is enabled. */
	public void onTick() {
	}

	protected <T extends Setting<?>> T register(T setting) {
		settings.add(setting);
		return setting;
	}

	protected BooleanSetting bool(String name, String id, boolean defaultValue) {
		return register(new BooleanSetting(name, id, defaultValue));
	}

	protected NumberSetting number(String name, String id, double defaultValue, double min, double max, double step) {
		return register(new NumberSetting(name, id, defaultValue, min, max, step));
	}
}
