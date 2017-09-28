package se.lth.cs.tycho.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ListSetting<T> implements Setting<List<T>> {
	private final Setting<T> setting;
	private final String separator;

	public ListSetting(Setting<T> setting, String separator) {
		this.setting = setting;
		this.separator = separator;
		assert !setting.read(separator).isPresent();
	}
	@Override
	public String getKey() {
		return setting.getKey();
	}

	@Override
	public String getType() {
		return "(" + setting.getType() + ") "+separator+" ...";
	}

	@Override
	public String getDescription() {
		return setting.getDescription();
	}

	@Override
	public Optional<List<T>> read(String text) {
		List<T> result = new ArrayList<>();
		int from = 0;
		while (from < text.length()) {
			int to = text.indexOf(separator, from);
			if (to < 0) {
				to = text.length();
			}
			String part = text.substring(from, to);
			Optional<T> partResult = setting.read(part);
			if (partResult.isPresent()) {
				result.add(partResult.get());
			} else {
				return Optional.empty();
			}
			from = to + separator.length();
		}
		return Optional.of(result);
	}

	@Override
	public List<T> defaultValue(Configuration configuration) {
		return Collections.emptyList();
	}
}
