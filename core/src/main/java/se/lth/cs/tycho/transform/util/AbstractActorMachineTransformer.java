package se.lth.cs.tycho.transform.util;

import java.lang.invoke.MethodHandle;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ConditionVisitor;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class AbstractActorMachineTransformer<P> extends AbstractBasicTransformer<P> implements ActorMachineTransformer<P>, ConditionVisitor<Condition, P>{

	private static final MethodHandle transInputPort = methodHandle(AbstractActorMachineTransformer.class, PortDecl.class, "transformInputPort");
	private static final MethodHandle transOutputPort = methodHandle(AbstractActorMachineTransformer.class, PortDecl.class, "transformOutputPort");
	private static final MethodHandle transTransition = methodHandle(AbstractActorMachineTransformer.class, Transition.class, "transformTransition");
	private static final MethodHandle transCondition = methodHandle(AbstractActorMachineTransformer.class, Condition.class, "transformCondition");
	private static final MethodHandle transVarDecl = methodHandle(AbstractActorMachineTransformer.class, LocalVarDecl.class, "transformVarDecl");
	private static final MethodHandle transScope = methodHandle(AbstractActorMachineTransformer.class, Scope.class, "transformScope");

	@Override
	public ActorMachine transformActorMachine(ActorMachine actorMachine, P param) {
		return actorMachine.copy(
				transformInputPorts(actorMachine.getInputPorts(), param), 
				transformOutputPorts(actorMachine.getOutputPorts(), param), 
				transformScopes(actorMachine.getScopes(), param), 
				actorMachine.getController(), 
				transformTransitions(actorMachine.getTransitions(), param), 
				transformConditions(actorMachine.getConditions(), param)
				);
	}

	@Override
	public PortDecl transformInputPort(PortDecl port, P param) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param) {
		return transformList(transInputPort, port, param);
	}

	@Override
	public PortDecl transformOutputPort(PortDecl port, P param) {
		return port.copy(port.getName(), transformTypeExpr(port.getType(), param));
	}

	@Override
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param) {
		return transformList(transOutputPort, port, param);
	}

	@Override
	public ImmutableList<Scope> transformScopes(
			ImmutableList<Scope> scopes, P param) {
		return transformList(transScope, scopes, param);
	}
	
	@Override
	public Scope transformScope(Scope scope, P param) {
		return scope.copy(transformList(transVarDecl, scope.getDeclarations(), param));
	}

	@Override
	public ImmutableList<Transition> transformTransitions(ImmutableList<Transition> transition, P param) {
		return transformList(transTransition, transition, param);
	}

	@Override
	public Transition transformTransition(Transition transition, P param) {
		return transition.copy(transition.getInputRates(), transition.getOutputRates(), transition.getScopesToKill(), transformStatement(transition.getBody(), param));
	}

	@Override
	public ImmutableList<Condition> transformConditions(ImmutableList<Condition> cond, P param) {
		return transformList(transCondition, cond, param);
	}

	@Override
	public Condition transformCondition(Condition cond, P param) {
		if(cond == null){
			return null;
		}
		return cond.accept(this, param);
	}

	@Override
	public Condition visitInputCondition(PortCondition c, P p) {
		return c.copy(transformPort(c.getPortName(), p), c.N(), c.isInputCondition());
	}

	@Override
	public Condition visitOutputCondition(PortCondition c, P p) {
		return c.copy(transformPort(c.getPortName(), p), c.N(), c.isInputCondition());
	}

	@Override
	public Condition visitPredicateCondition(PredicateCondition c, P p) {
		return c.copy(transformExpression(c.getExpression(), p));
	}

}
