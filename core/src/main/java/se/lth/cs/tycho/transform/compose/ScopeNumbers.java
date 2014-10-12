package se.lth.cs.tycho.transform.compose;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.util.ImmutableList;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class ScopeNumbers extends Module<ScopeNumbers.Decls> {

	public interface Decls {

		@Synthesized
		int numberOfScopes(Object content);

		@Inherited
		int scopeOffset(IRNode node , ActorMachine am);

		@Inherited
		int lookupScopeNumber(Object node, int scopeId);

		@Synthesized
		Variable translateVariable(Variable var);

		@Inherited
		ActorMachine actorMachine(IRNode node);
		
		@Synthesized
		public ImmutableList<Integer> scopesToKill(Transition transition);
	}
	
	public int numberOfScopes(ActorMachine actorMachine) {
		return actorMachine.getScopes().size();
	}
	
	public int scopeOffset(Network net, ActorMachine actorMachine) {
		int offset = 0;
		for (Node n : net.getNodes()) {
			if (n.getContent() == actorMachine) {
				return offset;
			}
			offset += e().numberOfScopes(n.getContent());
		}
		throw new Error();
	}
	
	public int lookupScopeNumber(ActorMachine am, int scope) {
		return e().scopeOffset(am, am) + scope;
	}
	
	public Variable translateVariable(Variable var) {
		if (var.isScopeVariable()) {
			int id = e().lookupScopeNumber(var.getIdentifier(), var.getScopeId());
			return var.copy(var.getName(), id);
		} else {
			return var;
		}
	}
	
	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}
	
	public ImmutableList<Integer> scopesToKill(Transition transition) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ActorMachine am = e().actorMachine(transition);
		int offset = e().scopeOffset(transition, am);
		for (int s : transition.getScopesToKill()) {
			builder.add(offset + s);
		}
		return builder.build();
	}

}
