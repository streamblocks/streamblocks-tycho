package se.lth.cs.tycho.backend.c.att;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
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
		if (decl != null && decl instanceof LocalVarDecl) {
			LocalVarDecl declVar = (LocalVarDecl) decl;
			if (!declVar.isAssignable()) {
				return e().constantInteger(declVar.getInitialValue());
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