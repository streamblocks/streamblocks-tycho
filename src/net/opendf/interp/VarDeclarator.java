package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.DeclVisitor;

public class VarDeclarator implements DeclVisitor<Integer, Environment> {
	
	private final Interpreter interpreter;
	
	public VarDeclarator(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public Integer visitDeclEntity(DeclEntity d, Environment p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer visitDeclType(DeclType d, Environment p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer visitDeclVar(DeclVar d, Environment env) {
		if (d.isVariableOnStack()) {
			interpreter.getStack().push(interpreter.evaluate(d.getInitialValue(), env));
			return 1;
		} else {
			Ref r = env.getMemory().declare(d.getVariablePosition());
			interpreter.evaluate(d.getInitialValue(), env).assignTo(r);
			return 0;
		}
	}

}
