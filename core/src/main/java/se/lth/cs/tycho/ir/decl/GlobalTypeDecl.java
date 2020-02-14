package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GlobalTypeDecl extends TypeDecl implements GlobalDecl {

	private Availability availability;
	private List<TaggedTupleDecl> tuples;

	public GlobalTypeDecl(Availability availability, String name, List<TaggedTupleDecl> tuples) {
		this(null, availability, name, tuples);
	}

	protected GlobalTypeDecl(TypeDecl original, Availability availability, String name, List<TaggedTupleDecl> tuples) {
		super(original, name);
		this.availability = availability;
		this.tuples = ImmutableList.from(tuples);
	}

	public List<TaggedTupleDecl> getTuples() {
		return tuples;
	}

	public GlobalTypeDecl withTuples(List<TaggedTupleDecl> tuples) {
		return copy(getAvailability(), getName(), tuples);
	}

	@Override
	public GlobalTypeDecl withName(String name) {
		return copy(getAvailability(), name, getTuples());
	}

	public Availability getAvailability() {
		return availability;
	}

	@Override
	public GlobalTypeDecl withAvailability(Availability availability) {
		return copy(availability, getName(), getTuples());
	}

	private GlobalTypeDecl copy(Availability availability, String name, List<TaggedTupleDecl> tuples) {
		if (availability == getAvailability() && Objects.equals(name, getName()) && Lists.sameElements(tuples, getTuples())) {
			return this;
		} else {
			return new GlobalTypeDecl(this, availability, name, tuples);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		tuples.forEach(action);
	}

	@Override
	public GlobalTypeDecl transformChildren(Transformation transformation) {
		return copy(
				getAvailability(),
				getName(),
				getTuples() == null ? null : transformation.mapChecked(TaggedTupleDecl.class, getTuples()));
	}
}
