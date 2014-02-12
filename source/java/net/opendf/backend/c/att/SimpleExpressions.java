package net.opendf.backend.c.att;

import net.opendf.ir.common.*;
import javarag.Module;
import javarag.Synthesized;

public class SimpleExpressions extends Module<SimpleExpressions.Required> {

	public interface Required {

		String functionApplication(Expression function, ExprApplication application);

		String parenthesizedExpression(Expression expression);

		String simpleExpression(Expression expression);

		String variableName(Variable variable);

	}

	@Synthesized
	public String simpleExpression(ExprApplication expr) {
		return get().functionApplication(expr.getFunction(), expr);
	}

	@Synthesized
	public String simpleExpression(ExprIf expr) {
		return
			get().parenthesizedExpression(expr.getCondition()) + " ? " +
			get().parenthesizedExpression(expr.getThenExpr()) +  " : " +
			get().parenthesizedExpression(expr.getElseExpr());
	}

	@Synthesized
	public String simpleExpression(ExprIndexer expr) {
		return
			get().simpleExpression(expr.getStructure()) + "[" +
			get().simpleExpression(expr.getIndex()) + "]";
	}

	@Synthesized
	public String simpleExpression(ExprLiteral expr) {
		switch (expr.getKind()) {
			case Integer: return expr.getText();
			case True: return "true";
			case False: return "false";
			default: return null;
		}
	}

	@Synthesized
	public String simpleExpression(ExprVariable expr) {
		return get().variableName(expr.getVariable());
	}

	@Synthesized
	public String parenthesizedExpression(Expression expr) {
		return get().simpleExpression(expr);
	}

	@Synthesized
	public String parenthesizedExpression(ExprIf expr) {
		return "(" + get().simpleExpression(expr) + ")";
	}

	@Synthesized
	public String parenthesizedExpression(ExprApplication expr) {
		return "(" + get().simpleExpression(expr) + ")";
	}
	
	@Synthesized
	public String simpleExpression(Expression expr) {
		return null;
	}

}