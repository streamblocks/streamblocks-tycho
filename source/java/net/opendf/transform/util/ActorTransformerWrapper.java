package net.opendf.transform.util;

import net.opendf.ir.Field;
import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.Port;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.Variable;
import net.opendf.ir.decl.LocalTypeDecl;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.decl.VarDecl;
import net.opendf.ir.expr.ExprApplication;
import net.opendf.ir.expr.ExprBinaryOp;
import net.opendf.ir.expr.ExprField;
import net.opendf.ir.expr.ExprIf;
import net.opendf.ir.expr.ExprIndexer;
import net.opendf.ir.expr.ExprInput;
import net.opendf.ir.expr.ExprLambda;
import net.opendf.ir.expr.ExprLet;
import net.opendf.ir.expr.ExprList;
import net.opendf.ir.expr.ExprLiteral;
import net.opendf.ir.expr.ExprMap;
import net.opendf.ir.expr.ExprProc;
import net.opendf.ir.expr.ExprSet;
import net.opendf.ir.expr.ExprUnaryOp;
import net.opendf.ir.expr.ExprVariable;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.StmtAssignment;
import net.opendf.ir.stmt.StmtBlock;
import net.opendf.ir.stmt.StmtCall;
import net.opendf.ir.stmt.StmtConsume;
import net.opendf.ir.stmt.StmtForeach;
import net.opendf.ir.stmt.StmtIf;
import net.opendf.ir.stmt.StmtOutput;
import net.opendf.ir.stmt.StmtWhile;
import net.opendf.ir.stmt.lvalue.LValue;
import net.opendf.ir.stmt.lvalue.LValueField;
import net.opendf.ir.stmt.lvalue.LValueIndexer;
import net.opendf.ir.stmt.lvalue.LValueVariable;
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
	public final LocalVarDecl transformVarDecl(LocalVarDecl varDecl, P param) {
		return inner.transformVarDecl(varDecl, param);
	}

	@Override
	public final ImmutableList<LocalVarDecl> transformVarDecls(ImmutableList<LocalVarDecl> varDecl, P param) {
		return inner.transformVarDecls(varDecl, param);
	}

	@Override
	public final LocalTypeDecl transformTypeDecl(LocalTypeDecl typeDecl, P param) {
		return inner.transformTypeDecl(typeDecl, param);
	}

	@Override
	public final ImmutableList<LocalTypeDecl> transformTypeDecls(ImmutableList<LocalTypeDecl> typeDecl, P param) {
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
