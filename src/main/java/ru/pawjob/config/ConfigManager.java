package ru.pawjob.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import ru.pawjob.PawjobClient;
import ru.pawjob.module.Module;
import ru.pawjob.module.setting.Setting;

/**
 * Saves and restores module state, keybinds and settings to config/pawjob.json.
 */
public class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path path = FabricLoader.getInstance().getConfigDir().resolve(PawjobClient.MOD_ID + ".json");

	public void load() {
		if (!Files.exists(path)) {
			return;
		}

		try {
			String raw = Files.readString(path);
			JsonObject root = GSON.fromJson(raw, JsonObject.class);

			if (root == null || !root.has("modules") || !root.get("modules").isJsonArray()) {
				return;
			}

			for (JsonElement element : root.getAsJsonArray("modules")) {
				if (!element.isJsonObject()) {
					continue;
				}

				JsonObject object = element.getAsJsonObject();

				if (!object.has("name")) {
					continue;
				}

				Module module = PawjobClient.getInstance().getModules().getModule(object.get("name").getAsString());

				if (module == null) {
					continue;
				}

				if (object.has("enabled")) {
					module.setEnabled(object.get("enabled").getAsBoolean());
				}

				if (object.has("key")) {
					module.setKey(object.get("key").getAsInt());
				}

				if (object.has("settings") && object.get("settings").isJsonObject()) {
					JsonObject settings = object.getAsJsonObject("settings");

					for (Setting<?> setting : module.getSettings()) {
						if (settings.has(setting.getId())) {
							setting.fromJson(settings.get(setting.getId()));
						}
					}
				}
			}
		} catch (Exception e) {
			PawjobClient.LOGGER.error("[pawjob] failed to load config", e);
		}
	}

	public void save() {
		JsonObject root = new JsonObject();
		JsonArray modules = new JsonArray();

		for (Module module : PawjobClient.getInstance().getModules().getModules()) {
			JsonObject object = new JsonObject();
			object.addProperty("name", module.getName());
			object.addProperty("enabled", module.isEnabled());
			object.addProperty("key", module.getKey());

			JsonObject settings = new JsonObject();

			for (Setting<?> setting : module.getSettings()) {
				settings.add(setting.getId(), setting.toJson());
			}

			object.add("settings", settings);
			modules.add(object);
		}

		root.add("modules", modules);

		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(root));
		} catch (IOException e) {
			PawjobClient.LOGGER.error("[pawjob] failed to save config", e);
		}
	}
}
