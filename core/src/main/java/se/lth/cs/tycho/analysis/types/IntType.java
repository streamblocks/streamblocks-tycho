package se.lth.cs.tycho.analysis.types;

import java.util.OptionalInt;

public class IntType implements Type {
	private static final LeastUpperBound leastUpperBound = new LeastUpperBound();
	private static final GreatestLowerBound greatestLowerBound = new GreatestLowerBound();

	private final OptionalInt size;
	private final boolean signed;

	public IntType(boolean signed) {
		this(signed, OptionalInt.empty());
	}

	public IntType(boolean signed, int size) {
		this(signed, OptionalInt.of(size));
	}

	private IntType(boolean signed, OptionalInt size) {
		this.signed = signed;
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
		return visitor.visitIntType(this, param);
	}

	private static class LeastUpperBound implements LeastUpperBoundVisitor<IntType> {
		@Override
		public Type visitIntType(IntType b, IntType a) {
			boolean signed = a.signed || b.signed;
			OptionalInt size;
			if (a.size.isPresent() && b.size.isPresent()) {
				int aSize = a.size.getAsInt();
				int bSize = b.size.getAsInt();
				if (!a.signed && b.signed) {
					aSize += 1;
				}
				if (!b.signed && a.signed) {
					bSize += 1;
				}
				size = OptionalInt.of(Math.max(aSize, bSize));
			} else {
				size = OptionalInt.empty();
			}
			return new IntType(signed, size);
		}
	}

	private static class GreatestLowerBound implements GreatestLowerBoundVisitor<IntType> {
		@Override
		public Type visitIntType(IntType b, IntType a) {
			OptionalInt size;
			if (a.size.isPresent() || b.size.isPresent()) {
				int aSize = a.size.orElse(Integer.MAX_VALUE);
				int bSize = b.size.orElse(Integer.MAX_VALUE);
				size = OptionalInt.of(Math.min(aSize, bSize));
			} else {
				size = OptionalInt.empty();
			}
			boolean signed = a.signed && b.signed;
			return new IntType(signed, size);
		}
	}

}
