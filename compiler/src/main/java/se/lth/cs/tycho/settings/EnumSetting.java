package se.lth.cs.tycho.settings;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EnumSetting<E extends Enum<E>> implements Setting<E> {
	private final Class<E> enumType;
	public EnumSetting(Class<E> enumType) {
		this.enumType = enumType;
	}

	@Override
	public String getType() {
		return Stream.of(constants())
				.map(this::settingName)
				.collect(Collectors.joining(" | "));
	}

	public E[] constants() {
		return enumType.getEnumConstants();
	}

	private String settingName(E variant) {
		return variant.name().toLowerCase().replace("_","-");
	}

	@Override
	public Optional<E> read(String name) {
		return Stream.of(constants())
				.filter(variant -> settingName(variant).equals(name))
				.findFirst();
	}

}
