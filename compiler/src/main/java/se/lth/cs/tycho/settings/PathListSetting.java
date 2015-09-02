package se.lth.cs.tycho.settings;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PathListSetting implements Setting<List<Path>> {
	@Override
	public Optional<List<Path>> read(String string) {
		try {
			return Optional.of(Stream.of(string.split(":")).map(Paths::get).collect(Collectors.toList()));
		} catch (InvalidPathException e) {
			return Optional.empty();
		}
	}
	public String getType() {
		return "path-list";
	}
}
