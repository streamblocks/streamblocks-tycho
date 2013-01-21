package net.opendf.util;

import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVisitor;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.common.StmtWhile;

public class AbstractStatementTraverser<T> implements StatementVisitor<T, T> {

	protected ExpressionVisitor<T, T> exprVisitor = null;
	protected DeclVisitor<T, T> declVisitor = null;
	protected AbstractGeneratorFilterTraverser<T> genTraverser = null;

	protected T traverseStmt(Statement s, T p) {
		return s.accept(this, p);
	}

	protected T traverseStmts(Statement[] stmts, T p) {
		for (Statement s : stmts) {
			p = s.accept(this, p);
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

	protected T traverseExpr(Expression e, T p) {
		if (exprVisitor != null) {
			p = e.accept(exprVisitor, p);
		}
		return p;
	}

	protected T traverseExprs(Expression[] exprs, T p) {
		if (exprVisitor != null) {
			for (Expression e : exprs) {
				p = e.accept(exprVisitor, p);
			}
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

	@Override
	public T visitStmtAssignment(StmtAssignment s, T p) {
		p = traverseExprs(s.getLocation(), p);
		return traverseExpr(s.getVal(), p);
	}

	@Override
	public T visitStmtBlock(StmtBlock s, T p) {
		p = traverseDecls(s.getTypeDecls(), p);
		p = traverseDecls(s.getVarDecls(), p);
		return traverseStmts(s.getStatements(), p);
	}

	@Override
	public T visitStmtIf(StmtIf s, T p) {
		p = traverseExpr(s.getCondition(), p);
		p = traverseStmt(s.getThenBranch(), p);
		p = traverseStmt(s.getElseBranch(), p);
		return p;
	}

	@Override
	public T visitStmtCall(StmtCall s, T p) {
		p = traverseExpr(s.getProcedure(), p);
		return traverseExprs(s.getArgs(), p);
	}

	@Override
	public T visitStmtOutput(StmtOutput s, T p) {
		return traverseExprs(s.getValues(), p);
	}

	@Override
	public T visitStmtWhile(StmtWhile s, T p) {
		p = traverseExpr(s.getCondition(), p);
		return traverseStmt(s.getBody(), p);
	}

	@Override
	public T visitStmtForeach(StmtForeach s, T p) {
		p = traverseGens(s.getGenerators(), p);
		return traverseStmt(s.getBody(), p);
	}

}
