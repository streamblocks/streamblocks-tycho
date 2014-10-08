package se.lth.cs.tycho.ir.stmt.lvalue;

public interface LValueVisitor<R, P> {
	public R visitLValueVariable(LValueVariable lvalue, P parameter);
	public R visitLValueIndexer(LValueIndexer lvalue, P parameter);
	public R visitLValueField(LValueField lvalue, P parameter);
}
