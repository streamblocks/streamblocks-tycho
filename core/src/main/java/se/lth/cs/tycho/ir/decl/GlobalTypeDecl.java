package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private Availability availability;
	private List<RecordDecl> records;

	public GlobalTypeDecl(Availability availability, String name, List<RecordDecl> records) {
		this(null, availability, name, records);
	}

	protected GlobalTypeDecl(TypeDecl original, Availability availability, String name, List<RecordDecl> records) {
		super(original, name);
		this.availability = availability;
		this.records = ImmutableList.from(records);
	}

	public List<RecordDecl> getRecords() {
		return records;
	}

	public GlobalTypeDecl withStructures(List<RecordDecl> structures) {
		return copy(getAvailability(), getName(), structures);
	}

	@Override
	public GlobalTypeDecl withName(String name) {
		return copy(getAvailability(), name, getRecords());
	}

	public Availability getAvailability() {
		return availability;
	}

	@Override
	public GlobalTypeDecl withAvailability(Availability availability) {
		return copy(availability, getName(), getRecords());
	}

	private GlobalTypeDecl copy(Availability availability, String name, List<RecordDecl> structures) {
		if (availability == getAvailability() && Objects.equals(name, getName()) && Lists.sameElements(structures, getRecords())) {
			return this;
		} else {
			return new GlobalTypeDecl(this, availability, name, structures);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		records.forEach(action);
	}

	@Override
	public GlobalTypeDecl transformChildren(Transformation transformation) {
		return copy(
				getAvailability(),
				getName(),
				getRecords() == null ? null : transformation.mapChecked(RecordDecl.class, getRecords()));
	}
}
