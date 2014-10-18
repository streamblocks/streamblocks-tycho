package se.lth.cs.tycho.backend.c.att;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import javarag.Module;
import javarag.Synthesized;

public class ConstantPropagation extends Module<ConstantPropagation.Decls> {

	interface Decls {
		@Synthesized
		Integer constantInteger(Expression e);

		IRNode declaration(Variable v);

	}

	public Integer constantInteger(Expression expr) {
		return null;
	}

	public Integer constantInteger(ExprVariable var) {
		IRNode decl = e().declaration(var.getVariable());
		if (decl != null && decl instanceof VarDecl) {
			VarDecl declVar = (VarDecl) decl;
			if (declVar.isConstant()) {
				return e().constantInteger(declVar.getValue());
			}
		}
		return null;
	}

	public Integer constantInteger(ExprLiteral lit) {
		if (lit.getKind() == ExprLiteral.Kind.Integer) {
			try {
				return Integer.valueOf(lit.getText());
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

}