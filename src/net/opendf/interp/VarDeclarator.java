package net.opendf.interp;

import net.opendf.interp.values.Ref;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.DeclVisitor;

public class VarDeclarator implements DeclVisitor<Integer, Environment> {
	
	private final ProceduralExecutor exec;
	
	public VarDeclarator(ProceduralExecutor exec) {
		this.exec = exec;
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
			exec.getStack().push(exec.evaluate(d.getInitialValue(), env));
			return 1;
		} else {
			Ref r = env.getMemory().declare(d.getVariablePosition());
			exec.evaluate(d.getInitialValue(), env).assignTo(r);
			return 0;
		}
	}

}
