package net.opendf.transform.util;

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
import net.opendf.ir.common.Field;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.LValueField;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
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
import net.opendf.ir.util.ImmutableList;

public class ActorTransformerWrapper<P> extends AbstractActorTransformer<P> implements ActorTransformer<P> {

	private final BasicTransformer<P> inner;

	public ActorTransformerWrapper(BasicTransformer<P> inner) {
		this.inner = inner;
	}

	@Override
	public final Expression transformExpression(Expression expr, P param) {
		return inner.transformExpression(expr, param);
	}

	@Override
	public final ImmutableList<Expression> transformExpressions(ImmutableList<Expression> expr, P param) {
		return inner.transformExpressions(expr, param);
	}

	@Override
	public final Statement transformStatement(Statement stmt, P param) {
		return inner.transformStatement(stmt, param);
	}

	@Override
	public final ImmutableList<Statement> transformStatements(ImmutableList<Statement> stmt, P param) {
		return inner.transformStatements(stmt, param);
	}

	@Override
	public final LValue transformLValue(LValue lvalue, P param) {
		return inner.transformLValue(lvalue, param);
	}

	@Override
	public final DeclVar transformVarDecl(DeclVar varDecl, P param) {
		return inner.transformVarDecl(varDecl, param);
	}

	@Override
	public final ImmutableList<DeclVar> transformVarDecls(ImmutableList<DeclVar> varDecl, P param) {
		return inner.transformVarDecls(varDecl, param);
	}

	@Override
	public final DeclType transformTypeDecl(DeclType typeDecl, P param) {
		return inner.transformTypeDecl(typeDecl, param);
	}

	@Override
	public final ImmutableList<DeclType> transformTypeDecls(ImmutableList<DeclType> typeDecl, P param) {
		return inner.transformTypeDecls(typeDecl, param);
	}

	@Override
	public final ParDeclValue transformValueParameter(ParDeclValue valueParam, P param) {
		return inner.transformValueParameter(valueParam, param);
	}

	@Override
	public final ImmutableList<ParDeclValue> transformValueParameters(ImmutableList<ParDeclValue> valueParam, P param) {
		return inner.transformValueParameters(valueParam, param);
	}

	@Override
	public final ParDeclType transformTypeParameter(ParDeclType typeParam, P param) {
		return inner.transformTypeParameter(typeParam, param);
	}

	@Override
	public final ImmutableList<ParDeclType> transformTypeParameters(ImmutableList<ParDeclType> typeParam, P param) {
		return inner.transformTypeParameters(typeParam, param);
	}

	@Override
	public final GeneratorFilter transformGenerator(GeneratorFilter generator, P param) {
		return inner.transformGenerator(generator, param);
	}

	@Override
	public final ImmutableList<GeneratorFilter> transformGenerators(ImmutableList<GeneratorFilter> generator, P param) {
		return inner.transformGenerators(generator, param);
	}

	@Override
	public final Variable transformVariable(Variable var, P param) {
		return inner.transformVariable(var, param);
	}

	@Override
	public final Field transformField(Field field, P param) {
		return inner.transformField(field, param);
	}

	@Override
	public final Port transformPort(Port port, P param) {
		return inner.transformPort(port, param);
	}

	@Override
	public final TypeExpr transformTypeExpr(TypeExpr typeExpr, P param) {
		return inner.transformTypeExpr(typeExpr, param);
	}
	
	/****************************************************
	 * StatementVisitor
	 */
	@Override
    public final Statement visitStmtAssignment(StmtAssignment s, P p){
		return transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtBlock(StmtBlock s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtIf(StmtIf s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtCall(StmtCall s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtOutput(StmtOutput s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtConsume(StmtConsume s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtWhile(StmtWhile s, P p){
		return inner.transformStatement(s, p);
    }
	@Override
    public final Statement visitStmtForeach(StmtForeach s, P p){
		return inner.transformStatement(s, p);
    }

	/****************************************************
	 * ExpressionVisitor
	 */
	@Override
    public final Expression visitExprApplication(ExprApplication e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprBinaryOp(ExprBinaryOp e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprField(ExprField e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprIf(ExprIf e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprIndexer(ExprIndexer e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprInput(ExprInput e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprLambda(ExprLambda e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprLet(ExprLet e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprList(ExprList e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprLiteral(ExprLiteral e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprMap(ExprMap e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprProc(ExprProc e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprSet(ExprSet e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprUnaryOp(ExprUnaryOp e, P p){
    	return inner.transformExpression(e, p);
    }
	@Override
    public final Expression visitExprVariable(ExprVariable e, P p){
    	return inner.transformExpression(e, p);
    }

	/****************************************************
	 * LValueVisitor
	 */
	@Override
	public final LValue visitLValueVariable(LValueVariable lvalue, P p){
		return inner.transformLValue(lvalue, p);
	}
	@Override
	public final LValue visitLValueIndexer(LValueIndexer lvalue, P p){
		return inner.transformLValue(lvalue, p);
	}
	@Override
	public final LValue visitLValueField(LValueField lvalue, P p){
		return inner.transformLValue(lvalue, p);
	}
}
