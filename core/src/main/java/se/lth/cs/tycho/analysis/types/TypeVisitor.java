package se.lth.cs.tycho.analysis.types;

public interface TypeVisitor<R, P> {
	public R visitBottomType(BottomType type, P param);
	public R visitIntType(IntType type, P param);
	public R visitLambdaType(LambdaType type, P param);
	public R visitListType(ListType type, P param);
	public R visitProcType(ProcType type, P param);
	public R visitSimpleType(SimpleType type, P param);
	public R visitTopType(TopType type, P param);
	public R visitUserDefinedType(UserDefinedType type, P param);
}
