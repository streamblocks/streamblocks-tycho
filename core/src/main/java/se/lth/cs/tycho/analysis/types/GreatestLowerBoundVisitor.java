package se.lth.cs.tycho.analysis.types;

public interface GreatestLowerBoundVisitor<T extends Type> extends TypeVisitor<Type, T> {
	@Override
	public default Type visitBottomType(BottomType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitIntType(IntType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitLambdaType(LambdaType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitListType(ListType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitProcType(ProcType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitSimpleType(SimpleType b, T a) {
		return new BottomType();
	}

	@Override
	public default Type visitTopType(TopType b, T a) {
		return a;
	}

	@Override
	public default Type visitUserDefinedType(UserDefinedType b, T a) {
		return new BottomType();
	}

}
