package ru.pawjob.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	PLAYER("Player"),
	RENDER("Render"),
	WORLD("World"),
	MISC("Misc");

	private final String title;

	Category(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
