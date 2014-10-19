package se.lth.cs.tycho.analysis.types;

public class BottomType implements Type {
	@Override
	public Type leastUpperBound(Type that) {
		return that;
	}

	@Override
	public Type greatestLowerBound(Type that) {
		return this;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitBottomType(this, param);
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BottomType);
	}

}
