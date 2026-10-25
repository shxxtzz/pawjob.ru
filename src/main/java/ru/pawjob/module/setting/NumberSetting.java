package ru.pawjob.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;

	public NumberSetting(String name, String id, double defaultValue, double min, double max, double step) {
		super(name, id, defaultValue);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getStep() {
		return step;
	}

	/** Sets the value clamped into the allowed range and snapped to the step. */
	public void set(double value) {
		setValue(value);
	}

	@Override
	public void setValue(Double value) {
		if (value == null) {
			return;
		}

		double clamped = Math.max(min, Math.min(max, value));

		if (step > 0.0) {
			clamped = Math.round(clamped / step) * step;
		}

		super.setValue(clamped);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) {
			set(json.getAsDouble());
		}
	}
}
