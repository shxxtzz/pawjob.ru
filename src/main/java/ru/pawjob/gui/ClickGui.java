package ru.pawjob.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import ru.pawjob.PawjobClient;
import ru.pawjob.module.Category;
import ru.pawjob.module.Module;
import ru.pawjob.module.setting.BooleanSetting;
import ru.pawjob.module.setting.NumberSetting;
import ru.pawjob.module.setting.Setting;

/**
 * The ClickGUI: one panel per category, laid out in a grid.
 * Left click toggles a module, right click starts a keybind capture,
 * scrolling over a number setting changes its value.
 */
public class ClickGui extends Screen {
	private static final int PADDING = 6;
	private static final int PANEL_WIDTH = 132;
	private static final int PANEL_GAP = 6;
	private static final int HEADER_HEIGHT = 15;
	private static final int ROW_HEIGHT = 13;

	private static final int SCREEN_COLOR = 0xC8080A0E;
	private static final int PANEL_COLOR = 0xE0141820;
	private static final int HEADER_COLOR = 0xFF242B3D;
	private static final int TEXT_COLOR = 0xFFD8D8D8;
	private static final int MUTED_COLOR = 0xFF7C8698;
	private static final int ENABLED_COLOR = 0xFF55FF88;
	private static final int HOVER_COLOR = 0xFFFFFFFF;

	private final List<Hitbox> hitboxes = new ArrayList<>();

	private Module listening;

	public ClickGui() {
		super(Text.literal("pawjob"));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, SCREEN_COLOR);
		hitboxes.clear();

		context.drawTextWithShadow(this.textRenderer, "pawjob", PADDING, PADDING, ENABLED_COLOR);

		String hint = listening == null
				? "left click toggles / right click binds"
				: "press a key to bind, esc to clear";
		context.drawTextWithShadow(this.textRenderer, hint,
				this.width - PADDING - this.textRenderer.getWidth(hint), PADDING, MUTED_COLOR);

		Category[] categories = Category.values();

		int columns = Math.max(1, Math.min(categories.length,
				(this.width - PADDING * 2 + PANEL_GAP) / (PANEL_WIDTH + PANEL_GAP)));
		int rows = (categories.length + columns - 1) / columns;
		int[] rowHeights = new int[rows];

		for (int i = 0; i < categories.length; i++) {
			int row = i / columns;
			rowHeights[row] = Math.max(rowHeights[row], panelHeight(categories[i]));
		}

		int top = PADDING + 12;

		for (int row = 0; row < rows; row++) {
			int x = PADDING;

			for (int column = 0; column < columns; column++) {
				int index = row * columns + column;

				if (index >= categories.length) {
					break;
				}

				drawPanel(context, categories[index], x, top, panelHeight(categories[index]), mouseX, mouseY);
				x += PANEL_WIDTH + PANEL_GAP;
			}

			top += rowHeights[row] + PANEL_GAP;
		}

		super.render(context, mouseX, mouseY, delta);
	}

	private int panelHeight(Category category) {
		int rows = 0;

		for (Module module : PawjobClient.getInstance().getModules().getModules(category)) {
			rows++;

			if (!module.isEnabled()) {
				continue;
			}

			rows += module.getSettings().size();
		}

		return HEADER_HEIGHT + PADDING + Math.max(rows, 1) * ROW_HEIGHT;
	}

	private void drawPanel(DrawContext context, Category category, int x, int y, int height, int mouseX, int mouseY) {
		context.fill(x, y, x + PANEL_WIDTH, y + height, PANEL_COLOR);
		context.fill(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT, HEADER_COLOR);
		context.drawTextWithShadow(this.textRenderer, category.getTitle(), x + PADDING, y + 4, TEXT_COLOR);

		List<Module> modules = PawjobClient.getInstance().getModules().getModules(category);
		int rowY = y + HEADER_HEIGHT + PADDING;

		if (modules.isEmpty()) {
			context.drawTextWithShadow(this.textRenderer, "empty", x + PADDING, rowY + 2, MUTED_COLOR);
			return;
		}

		for (Module module : modules) {
			boolean hovered = isHovered(mouseX, mouseY, x, rowY, PANEL_WIDTH, ROW_HEIGHT);
			String label = module.isListeningForBind() ? module.getName() + " <key>" : module.getName();
			int color = module.isEnabled() ? ENABLED_COLOR : (hovered ? HOVER_COLOR : TEXT_COLOR);

			context.drawTextWithShadow(this.textRenderer, label, x + PADDING, rowY + 2, color);
			hitboxes.add(new Hitbox(x, rowY, x + PANEL_WIDTH, rowY + ROW_HEIGHT, module, null));
			rowY += ROW_HEIGHT;

			if (!module.isEnabled()) {
				continue;
			}

			for (Setting<?> setting : module.getSettings()) {
				String text;
				int settingColor;

				if (setting instanceof BooleanSetting booleanSetting) {
					text = setting.getName() + ": " + (booleanSetting.getValue() ? "on" : "off");
					settingColor = booleanSetting.getValue() ? ENABLED_COLOR : MUTED_COLOR;
				} else if (setting instanceof NumberSetting numberSetting) {
					text = setting.getName() + ": " + String.format(Locale.ROOT, "%.2f", numberSetting.getValue());
					settingColor = MUTED_COLOR;
				} else {
					continue;
				}

				context.drawTextWithShadow(this.textRenderer, text, x + PADDING + 6, rowY + 2, settingColor);
				hitboxes.add(new Hitbox(x, rowY, x + PANEL_WIDTH, rowY + ROW_HEIGHT, module, setting));
				rowY += ROW_HEIGHT;
			}
		}
	}

	private static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (Hitbox hitbox : hitboxes) {
			if (mouseX < hitbox.x1() || mouseX >= hitbox.x2() || mouseY < hitbox.y1() || mouseY >= hitbox.y2()) {
				continue;
			}

			if (hitbox.setting() != null) {
				// Number settings are changed with the scroll wheel, not with a click.
				if (hitbox.setting() instanceof BooleanSetting booleanSetting) {
					booleanSetting.toggle();
				}

				return true;
			}

			Module module = hitbox.module();

			if (module == null) {
				continue;
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				module.toggle();
			} else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				if (listening != null) {
					listening.setListeningForBind(false);
				}

				listening = module;
				module.setListeningForBind(true);
			}

			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (Hitbox hitbox : hitboxes) {
			if (!(hitbox.setting() instanceof NumberSetting numberSetting)) {
				continue;
			}

			if (mouseX < hitbox.x1() || mouseX >= hitbox.x2() || mouseY < hitbox.y1() || mouseY >= hitbox.y2()) {
				continue;
			}

			numberSetting.set(numberSetting.getValue() + verticalAmount * numberSetting.getStep());
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (listening != null) {
			listening.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
			listening.setListeningForBind(false);
			listening = null;
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed() {
		if (listening != null) {
			listening.setListeningForBind(false);
			listening = null;
		}

		PawjobClient.getInstance().getConfig().save();
		super.removed();
	}

	private record Hitbox(int x1, int y1, int x2, int y2, Module module, Setting<?> setting) {
	}
}
