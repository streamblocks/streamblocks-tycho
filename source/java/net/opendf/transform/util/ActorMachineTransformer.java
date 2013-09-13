package net.opendf.transform.util;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.util.ImmutableList;

public interface ActorMachineTransformer<P> extends BasicTransformer<P> {

	public ActorMachine transformActorMachine(ActorMachine actorMachine, P param);


	public PortDecl transformInputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param);

	public PortDecl transformOutputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param);


	ImmutableList<ImmutableList<DeclVar>> transformScopes(ImmutableList<ImmutableList<DeclVar>> scope, P param);
	
	ImmutableList<Transition> transformTransitions(ImmutableList<Transition> transition, P param); 
	Transition transformTransition(Transition transition, P param); 

	ImmutableList<Condition>transformConditions(ImmutableList<Condition> cond, P param);
	Condition transformCondition(Condition cond, P param);

}
