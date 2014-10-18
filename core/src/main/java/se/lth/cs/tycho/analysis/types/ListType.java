package se.lth.cs.tycho.analysis.types;

import java.util.OptionalInt;

public class ListType implements Type {
	private static final LeastUpperBound leastUpperBound = new LeastUpperBound();
	private static final GreatestLowerBound greatestLowerBound = new GreatestLowerBound();

	private final Type elements;
	private final OptionalInt size;

	public ListType(Type elements) {
		this(elements, OptionalInt.empty());
	}
	
	public ListType(Type elements, int size) {
		this(elements, OptionalInt.of(size));
	}

	private ListType(Type elements, OptionalInt size) {
		this.elements = elements;
		this.size = size;
	}

	@Override
	public Type leastUpperBound(Type that) {
		return that.accept(leastUpperBound, this);
	}
	
	@Override
	public Type greatestLowerBound(Type that) {
		return that.accept(greatestLowerBound, this);
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitListType(this, param);
	}

	private static class LeastUpperBound implements LeastUpperBoundVisitor<ListType> {
		@Override
		public Type visitListType(ListType b, ListType a) {
			Type lub = a.elements.leastUpperBound(b.elements);
			OptionalInt size;
			if (a.size.isPresent() && b.size.isPresent() && a.size.getAsInt() == b.size.getAsInt()) {
				size = a.size;
			} else {
				size = OptionalInt.empty();
			}
			return new ListType(lub, size);
		}
	}

	private static class GreatestLowerBound implements GreatestLowerBoundVisitor<ListType> {
		@Override
		public Type visitListType(ListType b, ListType a) {
			Type glb = a.elements.greatestLowerBound(b.elements);
			if (a.size.isPresent() && b.size.isPresent()) {
				if (a.size.getAsInt() == b.size.getAsInt()) {
					return new ListType(glb, a.size);
				} else {
					return new BottomType();
				}
			} else {
				return new ListType(glb);
			}
		}
	}

}
