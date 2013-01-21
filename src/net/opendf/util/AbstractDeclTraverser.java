package net.opendf.util;

import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.DeclVisitor;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;

public class AbstractDeclTraverser<T> implements DeclVisitor<T, T> {
	
	protected ExpressionVisitor<T,T> exprVisitor = null;
	
	protected T traverseExpr(Expression e, T p) {
		if (exprVisitor != null) {
			p = e.accept(exprVisitor, p);
		}
		return p;
	}
	
	protected T traverseDecls(Decl[] decls, T p) {
		for (Decl d : decls) {
			p = d.accept(this, p);
		}
		return p;
	}

	@Override
	public T visitDeclEntity(DeclEntity d, T p) {
		p = traverseDecls(d.getTypeDecls(), p);
		return traverseDecls(d.getVarDecls(), p);
	}

	@Override
	public T visitDeclType(DeclType d, T p) {
		return p;
	}

	@Override
	public T visitDeclVar(DeclVar d, T p) {
		Expression e = d.getInitialValue();
		return e == null ? p : traverseExpr(e, p);
	}

}
