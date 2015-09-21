package se.lth.cs.tycho.transform.copy;

import java.util.function.BiFunction;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
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
import se.lth.cs.tycho.ir.expr.ExpressionVisitor;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StatementVisitor;
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
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.ActorTransformer;
import se.lth.cs.tycho.transform.util.BasicTransformer;

public class Copy implements BasicTransformer<Void>, ActorTransformer<Void>, ExpressionVisitor<Expression, Void>,
		StatementVisitor<Statement, Void>, LValueVisitor<LValue, Void>, EntityVisitor<Entity, Void> {

	private static final Copy INSTANCE = new Copy();

	public static Copy transformer() {
		return INSTANCE;
	}

	public <A, P, B> ImmutableList<B> map(ImmutableList<A> input, BiFunction<A, P, B> func, P param) {
		return input.stream().map(a -> func.apply(a, param)).collect(ImmutableList.collector());
	}

	public <A extends IRNode, P, B extends IRNode> BiFunction<Parameter<A>, P, Parameter<B>> liftParameter(BiFunction<A, P, B> func) {
		return (Parameter<A> p1, P p2) -> new Parameter<>(p1.getName(), func.apply(p1.getValue(), p2));
	}

	public <A, B, P, X, Y> BiFunction<ImmutableEntry<A, B>, P, ImmutableEntry<X, Y>> liftEntry(
			BiFunction<A, P, X> keyFunc, BiFunction<B, P, Y> valFunc) {
		return (ImmutableEntry<A, B> entry, P p) -> ImmutableEntry.of(keyFunc.apply(entry.getKey(), p),
				valFunc.apply(entry.getValue(), p));
	}

	@Override
	public Expression transformExpression(Expression expr, Void param) {
		if (expr == null)
			return null;
		return expr.accept(this, null);
	}

	@Override
	public ImmutableList<Expression> transformExpressions(ImmutableList<Expression> expr, Void param) {
		if (expr == null)
			return null;
		return map(expr, this::transformExpression, param);
	}

	@Override
	public Statement transformStatement(Statement stmt, Void param) {
		if (stmt == null)
			return null;
		return stmt.accept(this, null);
	}

	@Override
	public ImmutableList<Statement> transformStatements(ImmutableList<Statement> stmt, Void param) {
		if (stmt == null)
			return null;
		return map(stmt, this::transformStatement, param);
	}

	@Override
	public LValue transformLValue(LValue lvalue, Void param) {
		if (lvalue == null)
			return null;
		return lvalue.accept(this, null);
	}

	@Override
	public VarDecl transformVarDecl(VarDecl varDecl, Void param) {
		if (varDecl == null)
			return null;
		switch (varDecl.getLocationKind()) {
		case GLOBAL:
			return VarDecl.global(varDecl.getAvailability(), transformTypeExpr(varDecl.getType(), param),
					varDecl.getName(), transformExpression(varDecl.getValue(), param));
		case LOCAL:
			return VarDecl.local(transformTypeExpr(varDecl.getType(), param), varDecl.getName(), varDecl.isConstant(),
					transformExpression(varDecl.getValue(), param));
		case PARAMETER:
			return VarDecl.parameter(transformTypeExpr(varDecl.getType(), param), varDecl.getName());
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public ImmutableList<VarDecl> transformVarDecls(ImmutableList<VarDecl> varDecl, Void param) {
		if (varDecl == null)
			return null;
		return map(varDecl, this::transformVarDecl, param);
	}

	@Override
	public TypeDecl transformTypeDecl(TypeDecl typeDecl, Void param) {
		if (typeDecl == null)
			return null;
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<TypeDecl> transformTypeDecls(ImmutableList<TypeDecl> typeDecl, Void param) {
		if (typeDecl == null)
			return null;
		return map(typeDecl, this::transformTypeDecl, param);
	}

	public EntityDecl transformEntityDecl(EntityDecl entityDecl, Void param) {
		if (entityDecl == null)
			return null;
		return EntityDecl.global(entityDecl.getAvailability(), entityDecl.getName(),
				transformEntity(entityDecl.getEntity(), param));
	}

	public Entity transformEntity(Entity entity, Void param) {
		if (entity == null)
			return null;
		return entity.accept(this, param);
	}

	@Override
	public VarDecl transformValueParameter(VarDecl valueParam, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<VarDecl> transformValueParameters(ImmutableList<VarDecl> valueParam, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TypeDecl transformTypeParameter(TypeDecl typeParam, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<TypeDecl> transformTypeParameters(ImmutableList<TypeDecl> typeParam, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GeneratorFilter transformGenerator(GeneratorFilter generator, Void param) {
		if (generator == null)
			return null;
		return new GeneratorFilter(transformVarDecls(generator.getVariables(), param), transformExpression(
				generator.getCollectionExpr(), param), transformExpressions(generator.getFilters(), param));
	}

	@Override
	public ImmutableList<GeneratorFilter> transformGenerators(ImmutableList<GeneratorFilter> generator, Void param) {
		if (generator == null)
			return null;
		return map(generator, this::transformGenerator, param);
	}

	@Override
	public Variable transformVariable(Variable var, Void param) {
		if (var == null)
			return null;
		if (var.isScopeVariable()) {
			return Variable.scopeVariable(var.getName(), var.getScopeId());
		} else {
			return Variable.variable(var.getName());
		}
	}

	@Override
	public Field transformField(Field field, Void param) {
		if (field == null)
			return null;
		return new Field(field.getName());
	}

	@Override
	public Port transformPort(Port port, Void param) {
		if (port == null)
			return null;
		if (port.hasLocation()) {
			return new Port(port.getName(), port.getOffset());
		} else {
			return new Port(port.getName());	
		}
	}

	@Override
	public TypeExpr transformTypeExpr(TypeExpr typeExpr, Void param) {
		if (typeExpr == null)
			return null;
		return new TypeExpr(typeExpr.getName(), map(typeExpr.getTypeParameters(),
				liftParameter(this::transformTypeExpr), param), map(typeExpr.getValueParameters(),
				liftParameter(this::transformExpression), param));
	}

	@Override
	public CalActor transformActor(CalActor calActor, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Action transformAction(Action action, Void param) {
		if (action == null) return null;
		return new Action(action.getID(), action.getTag(), transformInputPatterns(action.getInputPatterns(), param),
				transformOutputExpressions(action.getOutputExpressions(), param), transformTypeDecls(
						action.getTypeDecls(), param), transformVarDecls(action.getVarDecls(), param),
				transformExpressions(action.getGuards(), param), transformStatements(action.getBody(), param),
				transformExpression(action.getDelay(), param), transformExpressions(action.getPreconditions(), param),
				transformExpressions(action.getPostconditions(), param));
	}

	@Override
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, Void param) {
		if (actions == null) return null;
		return map(actions, this::transformAction, param);
	}

	@Override
	public InputPattern transformInputPattern(InputPattern input, Void param) {
		if (input == null) return null;
		return new InputPattern(transformPort(input.getPort(), param), transformVarDecls(input.getVariables(), param),
				transformExpression(input.getRepeatExpr(), param));
	}

	@Override
	public ImmutableList<InputPattern> transformInputPatterns(ImmutableList<InputPattern> inputs, Void param) {
		if (inputs == null) return null;
		return map(inputs, this::transformInputPattern, param);
	}

	@Override
	public OutputExpression transformOutputExpression(OutputExpression output, Void param) {
		if (output == null) return null;
		return new OutputExpression(transformPort(output.getPort(), param), transformExpressions(
				output.getExpressions(), param), transformExpression(output.getRepeatExpr(), param));
	}

	@Override
	public ImmutableList<OutputExpression> transformOutputExpressions(ImmutableList<OutputExpression> output, Void param) {
		if (output == null) return null;
		return map(output, this::transformOutputExpression, param);
	}

	@Override
	public ImmutableList<ImmutableList<QID>> transformPriorities(ImmutableList<ImmutableList<QID>> prios, Void param) {
		return prios;
	}

	@Override
	public ScheduleFSM transformSchedule(ScheduleFSM schedule, Void param) {
		if (schedule == null) return null;
		return new ScheduleFSM(transformScheduleTransitions(schedule.getTransitions(), param),
				schedule.getInitialState());
	}

	@Override
	public ProcessDescription transformProcessDescription(ProcessDescription process, Void param) {
		if (process == null) {
			return null;
		} else {
			return new ProcessDescription(transformStatements(process.getStatements(), param), process.isRepeated());
		}
	}

	@Override
	public Transition transformScheduleTransition(Transition transition, Void param) {
		if (transition == null) return null;
		return new Transition(transition.getSourceState(), transition.getDestinationState(), transition.getActionTags());
	}

	@Override
	public ImmutableList<Transition> transformScheduleTransitions(ImmutableList<Transition> transitions, Void param) {
		if (transitions == null) return null;
		return map(transitions, this::transformScheduleTransition, param);
	}

	@Override
	public QID transformTag(QID tag, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<QID> transformTags(ImmutableList<QID> tags, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PortDecl transformInputPort(PortDecl port, Void param) {
		if (port == null) return null;
		return new PortDecl(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, Void param) {
		if (port == null) return null;
		return map(port, this::transformInputPort, param);
	}

	@Override
	public PortDecl transformOutputPort(PortDecl port, Void param) {
		if (port == null) return null;
		return new PortDecl(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, Void param) {
		if (port == null) return null;
		return map(port, this::transformInputPort, param);
	}

	@Override
	public Expression visitExprApplication(ExprApplication e, Void p) {
		return new ExprApplication(transformExpression(e.getFunction(), p), transformExpressions(e.getArgs(), p));
	}

	@Override
	public Expression visitExprBinaryOp(ExprBinaryOp e, Void p) {
		return new ExprBinaryOp(e.getOperations(), transformExpressions(e.getOperands(), p));
	}

	@Override
	public Expression visitExprField(ExprField e, Void p) {
		return new ExprField(transformExpression(e.getStructure(), p), transformField(e.getField(), p));
	}

	@Override
	public Expression visitExprIf(ExprIf e, Void p) {
		return new ExprIf(transformExpression(e.getCondition(), p), transformExpression(e.getThenExpr(), p),
				transformExpression(e.getElseExpr(), p));
	}

	@Override
	public Expression visitExprIndexer(ExprIndexer e, Void p) {
		return new ExprIndexer(transformExpression(e.getStructure(), p), transformExpression(e.getIndex(), p));
	}

	@Override
	public Expression visitExprInput(ExprInput e, Void p) {
		if (e.hasRepeat()) {
			return new ExprInput(transformPort(e.getPort(), p), e.getOffset(), e.getRepeat(), e.getPatternLength());
		} else {
			return new ExprInput(transformPort(e.getPort(), p), e.getOffset());
		}
	}

	@Override
	public Expression visitExprLambda(ExprLambda e, Void p) {
		return new ExprLambda(transformTypeDecls(e.getTypeParameters(), p),
				transformVarDecls(e.getValueParameters(), p), transformExpression(e.getBody(), p), transformTypeExpr(
						e.getReturnType(), p));
	}

	@Override
	public Expression visitExprLet(ExprLet e, Void p) {
		return new ExprLet(transformTypeDecls(e.getTypeDecls(), p), transformVarDecls(e.getVarDecls(), p),
				transformExpression(e.getBody(), p));
	}

	@Override
	public Expression visitExprList(ExprList e, Void p) {
		return new ExprList(transformExpressions(e.getElements(), p), transformGenerators(e.getGenerators(), p));
	}

	@Override
	public Expression visitExprLiteral(ExprLiteral e, Void p) {
		return new ExprLiteral(e.getKind(), e.getText());
	}

	@Override
	public Expression visitExprMap(ExprMap e, Void p) {
		return new ExprMap(map(e.getMappings(), liftEntry(this::transformExpression, this::transformExpression), p),
				transformGenerators(e.getGenerators(), p));
	}

	@Override
	public Expression visitExprProc(ExprProc e, Void p) {
		return new ExprProc(transformTypeDecls(e.getTypeParameters(), p), transformVarDecls(e.getValueParameters(), p),
				transformStatement(e.getBody(), p));
	}

	@Override
	public Expression visitExprSet(ExprSet e, Void p) {
		return new ExprSet(transformExpressions(e.getElements(), p), transformGenerators(e.getGenerators(), p));
	}

	@Override
	public Expression visitExprUnaryOp(ExprUnaryOp e, Void p) {
		return new ExprUnaryOp(e.getOperation(), transformExpression(e.getOperand(), p));
	}

	@Override
	public Expression visitExprVariable(ExprVariable e, Void p) {
		return new ExprVariable(transformVariable(e.getVariable(), p));
	}

	@Override
	public Statement visitStmtAssignment(StmtAssignment s, Void p) {
		return new StmtAssignment(transformLValue(s.getLValue(), p), transformExpression(s.getExpression(), p));
	}

	@Override
	public Statement visitStmtBlock(StmtBlock s, Void p) {
		return new StmtBlock(transformTypeDecls(s.getTypeDecls(), p), transformVarDecls(s.getVarDecls(), p),
				transformStatements(s.getStatements(), p));
	}

	@Override
	public Statement visitStmtIf(StmtIf s, Void p) {
		return new StmtIf(transformExpression(s.getCondition(), p), transformStatement(s.getThenBranch(), p),
				transformStatement(s.getElseBranch(), p));
	}

	@Override
	public Statement visitStmtCall(StmtCall s, Void p) {
		return new StmtCall(transformExpression(s.getProcedure(), p), transformExpressions(s.getArgs(), p));
	}

	@Override
	public Statement visitStmtOutput(StmtOutput s, Void p) {
		if (s.hasRepeat()) {
			return new StmtOutput(transformExpressions(s.getValues(), p), transformPort(s.getPort(), p), s.getRepeat());
		} else {
			return new StmtOutput(transformExpressions(s.getValues(), p), transformPort(s.getPort(), p));
		}
	}

	@Override
	public Statement visitStmtConsume(StmtConsume s, Void p) {
		return new StmtConsume(transformPort(s.getPort(), p), s.getNumberOfTokens());
	}

	@Override
	public Statement visitStmtWhile(StmtWhile s, Void p) {
		return new StmtWhile(transformExpression(s.getCondition(), p), transformStatement(s.getBody(), p));
	}

	@Override
	public Statement visitStmtForeach(StmtForeach s, Void p) {
		return new StmtForeach(transformGenerators(s.getGenerators(), p), transformStatement(s.getBody(), p));
	}

	@Override
	public LValue visitLValueVariable(LValueVariable lvalue, Void parameter) {
		return new LValueVariable(transformVariable(lvalue.getVariable(), parameter));
	}

	@Override
	public LValue visitLValueIndexer(LValueIndexer lvalue, Void parameter) {
		return new LValueIndexer(transformLValue(lvalue.getStructure(), parameter), transformExpression(
				lvalue.getIndex(), parameter));
	}

	@Override
	public LValue visitLValueField(LValueField lvalue, Void parameter) {
		return new LValueField(transformLValue(lvalue.getStructure(), parameter), transformField(lvalue.getField(),
				parameter));
	}

	@Override
	public Entity visitCalActor(CalActor entity, Void param) {
		return new CalActor(transformTypeDecls(entity.getTypeParameters(), param), transformVarDecls(
				entity.getValueParameters(), param), transformTypeDecls(entity.getTypeDecls(), param),
				transformVarDecls(entity.getVarDecls(), param), transformInputPorts(entity.getInputPorts(), param),
				transformOutputPorts(entity.getOutputPorts(), param),
				transformActions(entity.getInitializers(), param), transformActions(entity.getActions(), param),
				transformSchedule(entity.getScheduleFSM(), param), transformProcessDescription(entity.getProcessDescription(), param),
				transformPriorities(entity.getPriorities(), param),	transformExpressions(entity.getInvariants(), param));
	}

	@Override
	public Entity visitNlNetwork(NlNetwork entity, Void param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity visitXDFNetwork(XDFNetwork entity, Void param) {
		// FIXME does not copy!!!!
		return entity;
	}

}
