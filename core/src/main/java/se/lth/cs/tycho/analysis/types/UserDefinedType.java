package se.lth.cs.tycho.analysis.types;

public class UserDefinedType implements Type {
	@Override
	public Type leastUpperBound(Type that) {
		throw new Error("User defined types are not yet supported.");
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitUserDefinedType(this, param);
	}
	
	@Override
	public Type greatestLowerBound(Type that) {
		throw new Error("User defined types are not yet supported.");
	}

}
