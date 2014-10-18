package se.lth.cs.tycho.analysis.types;

public interface Type {
	public Type leastUpperBound(Type that);
	public Type greatestLowerBound(Type that);

	public <R, P> R accept(TypeVisitor<R, P> visitor, P param);
}
