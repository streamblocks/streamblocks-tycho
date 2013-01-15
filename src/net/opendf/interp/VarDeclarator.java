package net.opendf.interp;

import java.nio.channels.UnsupportedAddressTypeException;

import net.opendf.interp.attributed.AttrDeclVar;
import net.opendf.interp.values.Ref;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.DeclVisitor;

public class VarDeclarator implements DeclVisitor<Integer, Environment>, Declarator {
	
	private final Simulator simulator;
	
	public VarDeclarator(Simulator simulator) {
		this.simulator = simulator;
	}

	@Override
	public int declare(Decl decl, Environment env) {
		return decl.accept(this, env);
	}

	@Override
	public Integer visitDeclEntity(DeclEntity d, Environment p) {
		throw new UnsupportedAddressTypeException();
	}

	@Override
	public Integer visitDeclType(DeclType d, Environment p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer visitDeclVar(DeclVar d, Environment env) {
		AttrDeclVar decl = assertDecorated(d, AttrDeclVar.class);
		if (decl.varOnStack()) {
			simulator.stack().push(simulator.evaluator().evaluate(decl.getInitialValue(), env));
			return 1;
		} else {
			Ref r = env.getMemory().declare(decl.varPosition());
			simulator.evaluator().evaluate(decl.getInitialValue(), env).assignTo(r);
			return 0;
		}
	}
	
	public <A, B extends A> B assertDecorated(A a, Class<B> c) {
		if (c.isInstance(a)) {
			return c.cast(a);
		} else {
			throw new IllegalArgumentException("Tree not decorated");
		}
	}

}
