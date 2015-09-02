package se.lth.cs.tycho.ir.stmt.lvalue;

public interface LValueVisitor<R, P> {
	default R visitLValue(LValue lvalue, P parameter) {
		return lvalue.accept(this, parameter);
	}
	R visitLValueVariable(LValueVariable lvalue, P parameter);
	R visitLValueIndexer(LValueIndexer lvalue, P parameter);
	R visitLValueField(LValueField lvalue, P parameter);
}
