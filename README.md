# pawjob.ru

A client-side Fabric mod base: module system, keybinds, ClickGUI, HUD and config persistence. It ships with no modules — the framework is there, you fill the tabs.

## Requirements

- JDK 21 (the build targets Java 21)
- Minecraft 1.21.1 with Fabric Loader + Fabric API to run the jar

Gradle itself is not needed: the wrapper is included.

## Build

```sh
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

The jar lands in `build/libs/pawjob-1.0.0.jar`. Drop it into `.minecraft/mods` together with Fabric API.

If `./gradlew` is not executable after unzipping, run `chmod +x gradlew` first.

To launch the game with the mod loaded while developing, use `./gradlew runClient`.

## Layout

```
src/main/java/ru/pawjob/
  PawjobClient.java            client entrypoint, keybinds, event wiring
  module/
    Module.java                base class: name, category, keybind, settings, hooks
    Category.java              the ClickGUI tabs
    ModuleManager.java         registry, ticking, keybind polling
    setting/
      Setting.java             abstract value with JSON round-trip
      BooleanSetting.java      on/off value
      NumberSetting.java       clamped, stepped numeric value
  gui/
    ClickGui.java              the tabbed panel GUI
    Hud.java                   array list of enabled modules
  config/
    ConfigManager.java         saves/loads config/pawjob.json
```

## Adding a module

Create a class extending `Module` and register it in `ModuleManager.init()`:

```java
package ru.pawjob.module.modules;

import ru.pawjob.module.Category;
import ru.pawjob.module.Module;
import ru.pawjob.module.setting.BooleanSetting;

public class ExampleModule extends Module {
	private final BooleanSetting announce = bool("Announce", "announce", true);

	public ExampleModule() {
		super("Example", "Does one thing every tick", Category.MISC);
	}

	@Override
	public void onEnable() {
		// called when switched on
	}

	@Override
	public void onTick() {
		if (announce.getValue()) {
			// runs on the client thread, once per tick, while enabled
		}
	}
}
```

Subclasses get three helpers from `Module`: `bool(name, id, default)`, `number(name, id, default, min, max, step)` and `register(setting)`. Both settings types save and load automatically.

Modules are toggled by their keybind and appear in the ClickGUI under their category. A module with no keybind starts disabled until you bind one.

## Controls

- **Right Shift** — open the ClickGUI (rebindable in Options → Controls, under "pawjob")
- **Left click** a module — toggle it
- **Right click** a module — capture a key to bind it to; **Esc** clears the bind
- **Scroll** over a number setting — change its value

## Config

Everything is stored in `config/pawjob.json` in your game directory: enabled state, keybinds and settings, keyed by module name. It saves when the GUI closes and when the game shuts down.

## Changing the Minecraft version

All version numbers live in `gradle.properties`. Bump `minecraft_version`, `yarn_mappings`, `loader_version` and `fabric_api_version` together, using the current numbers from https://fabricmc.net/develop. If you jump to a Minecraft release that renamed rendering or keybinding APIs, the affected imports are `HudRenderCallback`, `DrawContext` and `KeyBinding`.

## Status

The tree has not been compiled here — this device has no Java toolchain, so the first `./gradlew build` on your machine is the real test. Everything is written against the 1.21.1 Yarn/API surface, and the wrapper jar matches Gradle 8.8.
