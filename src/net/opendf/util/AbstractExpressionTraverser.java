package net.opendf.util;

import java.util.Map.Entry;

import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVisitor;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprEntry;
import net.opendf.ir.common.ExprIf;
import net.opendf.ir.common.ExprIndexer;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprMap;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.ExprSet;
import net.opendf.ir.common.ExprUnaryOp;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;

public class AbstractExpressionTraverser<T> implements ExpressionVisitor<T, T> {
	
	protected DeclVisitor<T, T> declVisitor = null;
	protected StatementVisitor<T, T> stmtVisitor = null; 
	protected AbstractGeneratorFilterTraverser<T> genTraverser = null;
	
	private T traverseExpr(Expression e, T p) {
		return e.accept(this, p);
	}
	
	protected T traverseExprs(Expression[] exprs, T p) {
		for (Expression e : exprs) {
			p = e.accept(this, p);
		}
		return p;
	}
	
	protected T traverseDecls(Decl[] decls, T p) {
		if (declVisitor != null) {
			for (Decl d : decls) {
				p = d.accept(declVisitor, p);
			}
		}
		return p;
	}
	
	protected T traverseStmts(Statement[] stmts, T p) {
		if (stmtVisitor != null) {
			for (Statement s : stmts) {
				p = s.accept(stmtVisitor, p);
			}
		}
		return p;
	}
	
	protected T traverseGens(GeneratorFilter[] gens, T p) {
		if (genTraverser != null) {
			for (GeneratorFilter g : gens) {
				p = genTraverser.visitGeneratorFilter(g, p);
			}
		}
		return p;
	}

	@Override
	public T visitExprApplication(ExprApplication e, T p) {
		p = traverseExpr(e.getFunction(), p);
		p = traverseExprs(e.getArgs(), p);
		return p;
	}

	@Override
	public T visitExprBinaryOp(ExprBinaryOp e, T p) {
		return traverseExprs(e.getOperands(), p);
	}

	@Override
	public T visitExprEntry(ExprEntry e, T p) {
		return traverseExpr(e.getEnclosingExpr(), p);
	}

	@Override
	public T visitExprIf(ExprIf e, T p) {
		p = traverseExpr(e.getCondition(), p);
		p = traverseExpr(e.getThenExpr(), p);
		p = traverseExpr(e.getElseExpr(), p);
		return p;
	}

	@Override
	public T visitExprIndexer(ExprIndexer e, T p) {
		p = traverseExpr(e.getStructure(), p);
		return traverseExprs(e.getLocation(), p);
	}

	@Override
	public T visitExprInput(ExprInput e, T p) {
		return p;
	}

	@Override
	public T visitExprLambda(ExprLambda e, T p) {
		p = traverseDecls(e.getTypeDecls(), p);
		p = traverseDecls(e.getVarDecls(), p);
		// TODO visitor for ParDecl?
		return traverseExpr(e.getBody(), p);
	}

	@Override
	public T visitExprLet(ExprLet e, T p) {
		p = traverseDecls(e.getTypeDecls(), p);
		p = traverseDecls(e.getVarDecls(), p);
		return traverseExpr(e.getBody(), p);
	}

	@Override
	public T visitExprList(ExprList e, T p) {
		p = traverseGens(e.getGenerators(), p);
		return traverseExprs(e.getElements(), p);
	}

	@Override
	public T visitExprLiteral(ExprLiteral e, T p) {
		return p;
	}

	@Override
	public T visitExprMap(ExprMap e, T p) {
		p = traverseGens(e.getGenerators(), p);
		for (Entry<Expression, Expression> x : e.getMappings()) {
			p = traverseExpr(x.getKey(), p);
			p = traverseExpr(x.getValue(), p);
		}
		return p;
	}

	@Override
	public T visitExprProc(ExprProc e, T p) {
		p = traverseDecls(e.getTypeDecls(), p);
		p = traverseDecls(e.getVarDecls(), p);
		// TODO visitor for ParDecl?
		return traverseStmts(e.getBody(), p);
	}

	@Override
	public T visitExprSet(ExprSet e, T p) {
		p = traverseGens(e.getGenerators(), p);
		return traverseExprs(e.getElements(), p);
	}

	@Override
	public T visitExprUnaryOp(ExprUnaryOp e, T p) {
		return traverseExpr(e.getOperand(), p);
	}

	@Override
	public T visitExprVariable(ExprVariable e, T p) {
		return p;
	}
}
