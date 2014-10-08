package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprMap;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.ExprSet;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class NetworkDefinitionTransformerWrapper<P> extends AbstractNetworkDefinitionTransformer<P> implements NetworkDefinitionTransformer<P> {

	protected final BasicTransformer<P> inner;

	public NetworkDefinitionTransformerWrapper(BasicTransformer<P> inner) {
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
