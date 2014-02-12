package net.opendf.backend.c.att;

import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Variable;

public class ConstantPropagation extends Module<ConstantPropagation.Required> {


	@Synthesized
	public Integer constantInteger(Expression expr) {
		return null;
	}

	@Synthesized
	public Integer constantInteger(ExprVariable var) {
		IRNode decl = get().declaration(var.getVariable());
		if (decl != null && decl instanceof DeclVar) {
			DeclVar declVar = (DeclVar) decl;
			if (!declVar.isAssignable()) {
				return get().constantInteger(declVar.getInitialValue());
			}
		}
		return null;
	}

	@Synthesized
	public Integer constantInteger(ExprLiteral lit) {
		if (lit.getKind() == ExprLiteral.Kind.Integer) {
			try {
				return Integer.valueOf(lit.getText());
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	interface Required {
		Integer constantInteger(Expression e);
		IRNode declaration(Variable v);

	}
	
}