package se.lth.cs.tycho.type;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Objects;

public class UserType implements Type {

	private final String name;
	private final ImmutableList<RecordType> records;

	public UserType(String name, ImmutableList<RecordType> records) {
		this.name = name;
		this.records = records;
	}

	public String getName() {
		return name;
	}

	public ImmutableList<RecordType> getRecords() {
		return records;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserType userType = (UserType) o;
		return getName().equals(userType.getName()) &&
				getRecords().equals(userType.getRecords());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getRecords());
	}
}
