package se.lth.cs.tycho.classifier.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.PortCondition;
import net.opendf.ir.entity.am.Transition;
import net.opendf.ir.Port;
import net.opendf.ir.entity.PortContainer;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.util.ImmutableList;

public class PortDeclModule extends Module<PortDeclModule.Decls> {
	public interface Decls {
		@Inherited
		PortContainer portContainer(IRNode node);

		@Inherited
		PortDecl lookupPort(IRNode node, Port port);

		@Synthesized
		PortDecl declaration(Port port);

		@Synthesized
		Map<PortDecl, Integer> inputRates(Transition t);

		@Synthesized
		Map<PortDecl, Integer> outputRates(Transition t);
}

	public PortContainer portContainer(ActorMachine actorMachine) {
		return actorMachine;
	}

	public PortDecl lookupPort(PortCondition cond, Port port) {
		PortContainer container = e().portContainer(cond);
		if (cond.isInputCondition()) {
			return lookupInPortList(container.getInputPorts(), port);
		} else {
			return lookupInPortList(container.getOutputPorts(), port);
		}
	}

	public PortDecl declaration(Port port) {
		return e().lookupPort(port, port);
	}

	public Map<PortDecl, Integer> inputRates(Transition t) {
		PortContainer container = e().portContainer(t);
		return tokenRates(container.getInputPorts(), t.getInputRates());
	}

	public Map<PortDecl, Integer> outputRates(Transition t) {
		PortContainer container = e().portContainer(t);
		return tokenRates(container.getOutputPorts(), t.getOutputRates());
	}

	private Map<PortDecl, Integer> tokenRates(ImmutableList<PortDecl> portDecls, Map<Port, Integer> rates) {
		Map<PortDecl, Integer> result = new HashMap<>();
		for (Entry<Port, Integer> entry : rates.entrySet()) {
			PortDecl decl = lookupInPortList(portDecls, entry.getKey());
			result.put(decl, entry.getValue());
		}
		return result;
	}

	private PortDecl lookupInPortList(ImmutableList<PortDecl> portList, Port port) {
		if (port.hasLocation()) {
			return portList.get(port.getOffset());
		} else {
			for (PortDecl decl : portList) {
				if (decl.getName().equals(port.getName())) {
					return decl;
				}
			}
		}
		return null;
	}

}
