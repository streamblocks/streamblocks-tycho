package net.opendf.util;

import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.DeclVisitor;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;
import net.opendf.ir.common.GeneratorFilter;

public class AbstractGeneratorFilterTraverser<T> {
	protected ExpressionVisitor<T, T> exprVisitor = null;
	protected DeclVisitor<T, T> declVisitor = null;
	
	public T visitGeneratorFilter(GeneratorFilter g, T p) {
		if (declVisitor != null) {
			for (DeclVar d : g.getVariables()) {
				p = d.accept(declVisitor, p);
			}
		}
		if (exprVisitor != null) {
			p = g.getCollectionExpr().accept(exprVisitor, p);
			for (Expression e : g.getFilters()) {
				p = e.accept(exprVisitor, p);
			}
		}
		return p;
	}

}
