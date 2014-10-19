package se.lth.cs.tycho.analysis.types;

public interface Type {
	public Type leastUpperBound(Type that);
	public Type greatestLowerBound(Type that);
	
	public default boolean isAssignableFrom(Type that) {
		return equals(leastUpperBound(that));
	}

	public <R, P> R accept(TypeVisitor<R, P> visitor, P param);
}
