package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Map;

import static se.lth.cs.tycho.phases.SimpleTraversalUtil.visitAll;

public class SimpleTraversal<P> implements ExpressionVisitor<Void, P>, StatementVisitor<Void, P>, LValueVisitor<Void, P> {
	@Override
	public Void visitLValueVariable(LValueVariable lvalue, P parameter) {
		return null;
	}

	@Override
	public Void visitLValueIndexer(LValueIndexer lvalue, P parameter) {
		visitLValue(lvalue.getStructure(), parameter);
		visitExpression(lvalue.getIndex(), parameter);
		return null;
	}

	@Override
	public Void visitLValueField(LValueField lvalue, P parameter) {
		visitLValue(lvalue.getStructure(), parameter);
		return null;
	}

	@Override
	public Void visitStmtAssignment(StmtAssignment s, P p) {
		visitLValue(s.getLValue(), p);
		visitExpression(s.getExpression(), p);
		return null;
	}

	@Override
	public Void visitStmtBlock(StmtBlock s, P p) {
		visitAll(this::visitTypeDecl, s.getTypeDecls(), p);
		visitAll(this::visitVarDecl, s.getVarDecls(), p);
		visitAll(this::visitStatement, s.getStatements(), p);
		return null;
	}

	@Override
	public Void visitStmtIf(StmtIf s, P p) {
		visitExpression(s.getCondition(), p);
		visitStatement(s.getThenBranch(), p);
		visitStatement(s.getElseBranch(), p);
		return null;
	}

	@Override
	public Void visitStmtCall(StmtCall s, P p) {
		visitExpression(s.getProcedure(), p);
		visitAll(this::visitExpression, s.getArgs(), p);
		return null;
	}

	@Override
	public Void visitStmtOutput(StmtOutput s, P p) {
		visitAll(this::visitExpression, s.getValues(), p);
		return null;
	}

	@Override
	public Void visitStmtConsume(StmtConsume s, P p) {
		return null;
	}

	@Override
	public Void visitStmtWhile(StmtWhile s, P p) {
		visitExpression(s.getCondition(), p);
		visitStatement(s.getBody(), p);
		return null;
	}

	@Override
	public Void visitStmtForeach(StmtForeach s, P p) {
		visitAll(this::visitGenerator, s.getGenerators(), p);
		visitStatement(s.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprApplication(ExprApplication e, P p) {
		visitExpression(e.getFunction(), p);
		visitAll(this::visitExpression, e.getArgs(), p);
		return null;
	}

	@Override
	public Void visitExprBinaryOp(ExprBinaryOp e, P p) {
		visitAll(this::visitExpression, e.getOperands(), p);
		return null;
	}

	@Override
	public Void visitExprField(ExprField e, P p) {
		visitExpression(e.getStructure(), p);
		return null;
	}

	@Override
	public Void visitExprIf(ExprIf e, P p) {
		visitExpression(e.getCondition(), p);
		visitExpression(e.getThenExpr(), p);
		visitExpression(e.getElseExpr(), p);
		return null;
	}

	@Override
	public Void visitExprIndexer(ExprIndexer e, P p) {
		visitExpression(e.getStructure(), p);
		visitExpression(e.getIndex(), p);
		return null;
	}

	@Override
	public Void visitExprInput(ExprInput e, P p) {
		return null;
	}

	@Override
	public Void visitExprLambda(ExprLambda e, P p) {
		visitAll(this::visitTypeDecl, e.getTypeParameters(), p);
		visitAll(this::visitVarDecl, e.getValueParameters(), p);
		visitTypeExpr(e.getReturnType(), p);
		visitExpression(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprLet(ExprLet e, P p) {
		visitAll(this::visitTypeDecl, e.getTypeDecls(), p);
		visitAll(this::visitVarDecl, e.getVarDecls(), p);
		visitExpression(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprList(ExprList e, P p) {
		visitAll(this::visitGenerator, e.getGenerators(), p);
		visitAll(this::visitExpression, e.getElements(), p);
		return null;
	}

	@Override
	public Void visitExprLiteral(ExprLiteral e, P p) {
		return null;
	}

	@Override
	public Void visitExprMap(ExprMap e, P p) {
		visitAll(this::visitGenerator, e.getGenerators(), p);
		for (Map.Entry<Expression, Expression> mapping : e.getMappings()) {
			visitExpression(mapping.getKey(), p);
			visitExpression(mapping.getValue(), p);
		}
		return null;
	}

	@Override
	public Void visitExprProc(ExprProc e, P p) {
		visitAll(this::visitTypeDecl, e.getTypeParameters(), p);
		visitAll(this::visitVarDecl, e.getValueParameters(), p);
		visitStatement(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprSet(ExprSet e, P p) {
		visitAll(this::visitGenerator, e.getGenerators(), p);
		visitAll(this::visitExpression, e.getElements(), p);
		return null;
	}

	@Override
	public Void visitExprUnaryOp(ExprUnaryOp e, P p) {
		visitExpression(e.getOperand(), p);
		return null;
	}

	@Override
	public Void visitExprVariable(ExprVariable e, P p) {
		return null;
	}

	@Override
	public Void visitExpression(Expression expr, P param) {
		if (expr != null) {
			expr.accept(this, param);
		}
		return null;
	}

	@Override
	public Void visitStatement(Statement stmt, P param) {
		if (stmt != null) {
			stmt.accept(this, param);
		}
		return null;
	}

	@Override
	public Void visitLValue(LValue lvalue, P param) {
		lvalue.accept(this, param);
		return null;
	}

	public Void visitVarDecl(VarDecl varDecl, P param) {
		visitTypeExpr(varDecl.getType(), param);
		visitExpression(varDecl.getValue(), param);
		return null;
	}

	public Void visitTypeDecl(TypeDecl typeDecl, P param) {
		return null;
	}

	public Void visitGenerator(GeneratorFilter generator, P param) {
		visitExpression(generator.getCollectionExpr(), param);
		visitAll(this::visitVarDecl, generator.getVariables(), param);
		visitAll(this::visitExpression, generator.getFilters(), param);
		return null;
	}

	public Void visitTypeExpr(TypeExpr typeExpr, P param) {
		if (typeExpr != null) {
			ImmutableList<Parameter<TypeExpr>> typeParameters = typeExpr.getTypeParameters();
			if (typeParameters != null) {
				for (Parameter<TypeExpr> typeParam : typeParameters) {
					visitTypeExpr(typeParam.getValue(), param);
				}
			}
			ImmutableList<Parameter<Expression>> valueParameters = typeExpr.getValueParameters();
			if (valueParameters != null) {
				for (Parameter<Expression> valParam : valueParameters) {
					visitExpression(valParam.getValue(), param);
				}
			}
		}
		return null;
	}

}
