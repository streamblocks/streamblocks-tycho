package net.opendf.analyze.util;

import java.util.Map.Entry;

import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprField;
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
import net.opendf.ir.common.Field;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.LValueField;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.LValueVisitor;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtConsume;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.common.StmtWhile;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;

public abstract class AbstractBasicTraverser<P> implements BasicTraverser<P>, ExpressionVisitor<Void, P>,
		StatementVisitor<Void, P>, LValueVisitor<Void, P> {

	@Override
	public Void visitLValueVariable(LValueVariable lvalue, P parameter) {
		traverseVariable(lvalue.getVariable(), parameter);
		return null;
	}

	@Override
	public Void visitLValueIndexer(LValueIndexer lvalue, P parameter) {
		traverseLValue(lvalue.getStructure(), parameter);
		traverseExpression(lvalue.getIndex(), parameter);
		return null;
	}

	@Override
	public Void visitLValueField(LValueField lvalue, P parameter) {
		traverseLValue(lvalue.getStructure(), parameter);
		traverseField(lvalue.getField(), parameter);
		return null;
	}

	@Override
	public Void visitStmtAssignment(StmtAssignment s, P p) {
		traverseLValue(s.getLValue(), p);
		traverseExpression(s.getExpression(), p);
		return null;
	}

	@Override
	public Void visitStmtBlock(StmtBlock s, P p) {
		traverseTypeDecls(s.getTypeDecls(), p);
		traverseVarDecls(s.getVarDecls(), p);
		traverseStatements(s.getStatements(), p);
		return null;
	}

	@Override
	public Void visitStmtIf(StmtIf s, P p) {
		traverseExpression(s.getCondition(), p);
		traverseStatement(s.getThenBranch(), p);
		traverseStatement(s.getElseBranch(), p);
		return null;
	}

	@Override
	public Void visitStmtCall(StmtCall s, P p) {
		traverseExpression(s.getProcedure(), p);
		traverseExpressions(s.getArgs(), p);
		return null;
	}

	@Override
	public Void visitStmtOutput(StmtOutput s, P p) {
		traversePort(s.getPort(), p);
		traverseExpressions(s.getValues(), p);
		return null;
	}

	@Override
	public Void visitStmtConsume(StmtConsume s, P p) {
		traversePort(s.getPort(), p);
		return null;
	}

	@Override
	public Void visitStmtWhile(StmtWhile s, P p) {
		traverseExpression(s.getCondition(), p);
		traverseStatement(s.getBody(), p);
		return null;
	}

	@Override
	public Void visitStmtForeach(StmtForeach s, P p) {
		traverseGenerators(s.getGenerators(), p);
		traverseStatement(s.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprApplication(ExprApplication e, P p) {
		traverseExpression(e.getFunction(), p);
		traverseExpressions(e.getArgs(), p);
		return null;
	}

	@Override
	public Void visitExprBinaryOp(ExprBinaryOp e, P p) {
		traverseExpressions(e.getOperands(), p);
		return null;
	}

	@Override
	public Void visitExprField(ExprField e, P p) {
		traverseExpression(e.getStructure(), p);
		traverseField(e.getField(), p);
		return null;
	}

	@Override
	public Void visitExprIf(ExprIf e, P p) {
		traverseExpression(e.getCondition(), p);
		traverseExpression(e.getThenExpr(), p);
		traverseExpression(e.getElseExpr(), p);
		return null;
	}

	@Override
	public Void visitExprIndexer(ExprIndexer e, P p) {
		traverseExpression(e.getStructure(), p);
		traverseExpression(e.getIndex(), p);
		return null;
	}

	@Override
	public Void visitExprInput(ExprInput e, P p) {
		traversePort(e.getPort(), p);
		return null;
	}

	@Override
	public Void visitExprLambda(ExprLambda e, P p) {
		traverseTypeParameters(e.getTypeParameters(), p);
		traverseValueParameters(e.getValueParameters(), p);
		traverseTypeExpr(e.getReturnType(), p);
		traverseExpression(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprLet(ExprLet e, P p) {
		traverseTypeDecls(e.getTypeDecls(), p);
		traverseVarDecls(e.getVarDecls(), p);
		traverseExpression(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprList(ExprList e, P p) {
		traverseGenerators(e.getGenerators(), p);
		traverseExpressions(e.getElements(), p);
		return null;
	}

	@Override
	public Void visitExprLiteral(ExprLiteral e, P p) {
		return null;
	}

	@Override
	public Void visitExprMap(ExprMap e, P p) {
		traverseGenerators(e.getGenerators(), p);
		for (Entry<Expression, Expression> mapping : e.getMappings()) {
			traverseExpression(mapping.getKey(), p);
			traverseExpression(mapping.getValue(), p);
		}
		return null;
	}

	@Override
	public Void visitExprProc(ExprProc e, P p) {
		traverseTypeParameters(e.getTypeParameters(), p);
		traverseValueParameters(e.getValueParameters(), p);
		traverseStatement(e.getBody(), p);
		return null;
	}

	@Override
	public Void visitExprSet(ExprSet e, P p) {
		traverseGenerators(e.getGenerators(), p);
		traverseExpressions(e.getElements(), p);
		return null;
	}

	@Override
	public Void visitExprUnaryOp(ExprUnaryOp e, P p) {
		traverseExpression(e.getOperand(), p);
		return null;
	}

	@Override
	public Void visitExprVariable(ExprVariable e, P p) {
		traverseVariable(e.getVariable(), p);
		return null;
	}

	@Override
	public void traverseExpression(Expression expr, P param) {
		if (expr != null) {
			expr.accept(this, param);
		}
	}

	@Override
	public void traverseExpressions(ImmutableList<Expression> expr, P param) {
		for (Expression e : expr) {
			traverseExpression(e, param);
		}
	}

	@Override
	public void traverseStatement(Statement stmt, P param) {
		if (stmt != null) {
			stmt.accept(this, param);
		}
	}

	@Override
	public void traverseStatements(ImmutableList<Statement> stmt, P param) {
		for (Statement s : stmt) {
			traverseStatement(s, param);
		}
	}

	@Override
	public void traverseLValue(LValue lvalue, P param) {
		lvalue.accept(this, param);
	}

	@Override
	public void traverseVarDecl(DeclVar varDecl, P param) {
		traverseTypeExpr(varDecl.getType(), param);
		traverseExpression(varDecl.getInitialValue(), param);
	}

	@Override
	public void traverseVarDecls(ImmutableList<DeclVar> varDecl, P param) {
		for (DeclVar v : varDecl) {
			traverseVarDecl(v, param);
		}
	}

	@Override
	public void traverseTypeDecl(DeclType typeDecl, P param) {
	}

	@Override
	public void traverseTypeDecls(ImmutableList<DeclType> typeDecl, P param) {
		for (DeclType d : typeDecl) {
			traverseTypeDecl(d, param);
		}
	}

	@Override
	public void traverseValueParameter(ParDeclValue valueParam, P param) {
		traverseTypeExpr(valueParam.getType(), param);
	}

	@Override
	public void traverseValueParameters(ImmutableList<ParDeclValue> valueParam, P param) {
		for (ParDeclValue p : valueParam) {
			traverseValueParameter(p, param);
		}
	}

	@Override
	public void traverseTypeParameter(ParDeclType typeParam, P param) {
	}

	@Override
	public void traverseTypeParameters(ImmutableList<ParDeclType> typeParam, P param) {
		for (ParDeclType p : typeParam) {
			traverseTypeParameter(p, param);
		}
	}

	@Override
	public void traverseGenerator(GeneratorFilter generator, P param) {
		traverseExpression(generator.getCollectionExpr(), param);
		traverseVarDecls(generator.getVariables(), param);
		traverseExpressions(generator.getFilters(), param);
	}

	@Override
	public void traverseGenerators(ImmutableList<GeneratorFilter> generator, P param) {
		for (GeneratorFilter gen : generator) {
			traverseGenerator(gen, param);
		}
	}

	@Override
	public void traverseVariable(Variable var, P param) {
	}

	@Override
	public void traverseField(Field field, P param) {
	}

	@Override
	public void traversePort(Port port, P param) {
	}

	@Override
	public void traverseTypeExpr(TypeExpr typeExpr, P param) {
		if (typeExpr == null)
			return;
		ImmutableList<ImmutableEntry<String, TypeExpr>> typeParameters = typeExpr.getTypeParameters();
		if (typeParameters != null) {
			for (Entry<String, TypeExpr> typeParam : typeParameters) {
				traverseTypeExpr(typeParam.getValue(), param);
			}
		}
		ImmutableList<ImmutableEntry<String, Expression>> valueParameters = typeExpr.getValueParameters();
		if (valueParameters != null) {
			for (Entry<String, Expression> valParam : valueParameters) {
				traverseExpression(valParam.getValue(), param);
			}
		}
	}

}
