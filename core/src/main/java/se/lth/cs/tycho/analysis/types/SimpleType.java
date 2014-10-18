package se.lth.cs.tycho.analysis.types;

public class SimpleType implements Type {
	private static final LeastUpperBound leastUpperBound = new LeastUpperBound();
	private static final GreatestLowerBound greatestLowerBound = new GreatestLowerBound();
	private final String name;

	public SimpleType(String name) {
		this.name = name;
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
		return visitor.visitSimpleType(this, param);
	}

	private static class LeastUpperBound implements LeastUpperBoundVisitor<SimpleType> {
		@Override
		public Type visitSimpleType(SimpleType b, SimpleType a) {
			if (a.name.equals(b.name)) {
				return a;
			} else {
				return new TopType();
			}
		}
	}
	
	private static class GreatestLowerBound implements GreatestLowerBoundVisitor<SimpleType> {
		@Override
		public Type visitSimpleType(SimpleType b, SimpleType a) {
			if (a.name.equals(b.name)){
				return a;
			} else {
				return new BottomType();
			}
		}
	}

}
