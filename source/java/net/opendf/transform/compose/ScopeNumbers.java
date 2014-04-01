package net.opendf.transform.compose;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.Variable;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;
import net.opendf.ir.util.ImmutableList;

public class ScopeNumbers extends Module<ScopeNumbers.Required> {

	public interface Required {

		int numberOfScopes(PortContainer content);

		int scopeOffset(IRNode node , ActorMachine am);

		int lookupScopeNumber(Object node, int scopeId);

		ActorMachine actorMachine(IRNode node);
		
	}
	
	@Synthesized
	public int numberOfScopes(ActorMachine actorMachine) {
		return actorMachine.getScopes().size();
	}
	
	@Inherited
	public int scopeOffset(Network net, ActorMachine actorMachine) {
		int offset = 0;
		for (Node n : net.getNodes()) {
			if (n.getContent() == actorMachine) {
				return offset;
			}
			offset += get().numberOfScopes(n.getContent());
		}
		throw new Error();
	}
	
	@Inherited
	public int lookupScopeNumber(ActorMachine am, int scope) {
		return get().scopeOffset(am, am) + scope;
	}
	
	@Synthesized
	public Variable translateVariable(Variable var) {
		if (var.isScopeVariable()) {
			int id = get().lookupScopeNumber(var.getIdentifier(), var.getScopeId());
			return var.copy(var.getName(), id);
		} else {
			return var;
		}
	}
	
	@Inherited
	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}
	
	@Synthesized
	public ImmutableList<Integer> scopesToKill(Transition transition) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ActorMachine am = get().actorMachine(transition);
		int offset = get().scopeOffset(transition, am);
		for (int s : transition.getScopesToKill()) {
			builder.add(offset + s);
		}
		return builder.build();
	}

}
