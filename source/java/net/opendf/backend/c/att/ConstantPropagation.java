package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.Variable;
import net.opendf.ir.common.decl.DeclVar;
import net.opendf.ir.common.expr.ExprLiteral;
import net.opendf.ir.common.expr.ExprVariable;
import net.opendf.ir.common.expr.Expression;

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
		if (decl != null && decl instanceof DeclVar) {
			DeclVar declVar = (DeclVar) decl;
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