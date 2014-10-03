package net.opendf.backend.c.att;

import net.opendf.ir.common.*;
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