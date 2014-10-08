package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface ActorMachineTransformer<P> extends BasicTransformer<P> {

	public ActorMachine transformActorMachine(ActorMachine actorMachine, P param);


	public PortDecl transformInputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param);

	public PortDecl transformOutputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param);


	ImmutableList<Scope> transformScopes(ImmutableList<Scope> scope, P param);
	Scope transformScope(Scope scope, P param);
	
	ImmutableList<Transition> transformTransitions(ImmutableList<Transition> transition, P param); 
	Transition transformTransition(Transition transition, P param); 

	ImmutableList<Condition>transformConditions(ImmutableList<Condition> cond, P param);
	Condition transformCondition(Condition cond, P param);

}
