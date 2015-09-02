package se.lth.cs.tycho.settings;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class PathSetting implements Setting<Path> {
	public Optional<Path> read(String value) {
		try {
			return Optional.of(Paths.get(value));
		} catch (InvalidPathException e) {
			return Optional.empty();
		}
	}
	public String getType() {
		return "path";
	}
}
