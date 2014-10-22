package se.lth.cs.tycho.analysis.types;

public class TopType implements Type {

	@Override
	public Type leastUpperBound(Type that) {
		return this;
	}
	
	@Override
	public Type greatestLowerBound(Type that) {
		return that;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitTopType(this, param);
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof TopType;
	}

}
