package ru.pawjob.module.setting;

import com.google.gson.JsonElement;

/**
 * A single configurable value owned by a {@link ru.pawjob.module.Module}.
 * Values are saved and loaded by the config manager under the module's name.
 */
public abstract class Setting<T> {
	private final String name;
	private final String id;
	private T value;

	protected Setting(String name, String id, T defaultValue) {
		this.name = name;
		this.id = id;
		this.value = defaultValue;
	}

	public String getName() {
		return name;
	}

	/** Stable key used in the config file. */
	public String getId() {
		return id;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement json);
}
