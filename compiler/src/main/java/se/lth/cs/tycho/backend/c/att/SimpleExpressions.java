package se.lth.cs.tycho.backend.c.att;

import java.util.Optional;

import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import javarag.Module;
import javarag.Synthesized;

public class SimpleExpressions extends Module<SimpleExpressions.Decls> {

	public interface Decls {

		@Synthesized
		String parenthesizedExpression(Expression expression);

		@Synthesized
		String simpleExpression(Expression expression);

		String functionApplication(Expression function, ExprApplication application);

		String variableName(Variable variable);

		Optional<Object> constant(Expression expr);

	}

	public String simpleExpression(ExprApplication expr) {
		return e().functionApplication(expr.getFunction(), expr);
	}

	public String simpleExpression(ExprIf expr) {
		return e().parenthesizedExpression(expr.getCondition()) + " ? " +
				e().parenthesizedExpression(expr.getThenExpr()) + " : " +
				e().parenthesizedExpression(expr.getElseExpr());
	}

	public String simpleExpression(ExprIndexer expr) {
		return e().simpleExpression(expr.getStructure()) + "[" +
				e().simpleExpression(expr.getIndex()) + "]";
	}

	public String simpleExpression(ExprLiteral expr) {
		switch (expr.getKind()) {
		case Integer:
			return expr.getText();
		case True:
			return "true";
		case False:
			return "false";
		default:
			return null;
		}
	}

	public String simpleExpression(ExprVariable expr) {
		Optional<Object> constant = e().constant(expr);
		if (constant.isPresent()) {
			Object c = constant.get();
			if (c instanceof Integer) {
				return c.toString();
			} else if (c instanceof Boolean) {
				return c.toString();
			}
		}
		return e().variableName(expr.getVariable());
	}

	public String parenthesizedExpression(Expression expr) {
		return e().simpleExpression(expr);
	}

	public String parenthesizedExpression(ExprIf expr) {
		return "(" + e().simpleExpression(expr) + ")";
	}

	public String parenthesizedExpression(ExprApplication expr) {
		return "(" + e().simpleExpression(expr) + ")";
	}

	public String simpleExpression(Expression expr) {
		return null;
	}

}