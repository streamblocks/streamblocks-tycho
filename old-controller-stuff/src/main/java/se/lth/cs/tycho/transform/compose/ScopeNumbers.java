package se.lth.cs.tycho.transform.compose;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
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
		ActorMachine actorMachine(IRNode node);
		
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

	public ActorMachine actorMachine(ActorMachine am) {
		return am;
	}
	
}
