package net.opendf.transform.compose;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javarag.Inherited;
import javarag.Memoized;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

@Memoized("portNameTranslator")
public class PortNames extends Module<PortNames.Required> {

	public interface Required {

		PortDecl declaration(Port port);

		String uniquePortName(PortDecl decl);

		String uniquePortName(Port port);

		Map<PortContainer, Map<PortDecl, String>> portNameTranslator(Network net);

		String lookupUniquePortName(IRNode node, PortContainer container, PortDecl decl);

		PortContainer lookupPortContainer(PortDecl decl);

		Object identifierOwner(Identifier identifier);

	}
	
	@Inherited
	public Object identifierOwner(Object owner) {
		return owner;
	}
	
	@Synthesized
	public String uniquePortName(Port port) {
		PortDecl decl = get().declaration(port);
		return get().uniquePortName(decl);
	}
	
	@Synthesized
	public String uniquePortName(PortDecl decl) {
		return get().lookupUniquePortName(decl, get().lookupPortContainer(decl), decl);
	}
	
	@Inherited
	public PortContainer lookupPortContainer(ActorMachine am) {
		return am;
	}
	
	@Synthesized
	public Map<PortContainer, Map<PortDecl, String>> portNameTranslator(Network net) {
		Set<String> names = new HashSet<>();
		Map<PortContainer, Map<PortDecl, String>> translator = new HashMap<>();
		for (Node node : net.getNodes()) {
			PortContainer pc = node.getContent();
			Map<PortDecl, String> map = new HashMap<>();
			translator.put(pc, map);
			for (PortDecl in : pc.getInputPorts()) {
				map.put(in, registerUniqueName(in.getName(), names));
			}
			for (PortDecl out : pc.getOutputPorts()) {
				map.put(out, registerUniqueName(out.getName(), names));
			}
		}
		return translator;
	}
	
	
	private String registerUniqueName(String originalName, Set<String> names) {
		String name = originalName;
		int i = 1;
		while (names.contains(name)) {
			i += 1;
			name = originalName + i;
		}
		names.add(name);
		return name;
	}

	@Inherited
	public String lookupUniquePortName(Network net, PortContainer pc, PortDecl decl) {
		Map<PortContainer, Map<PortDecl, String>> translator = get().portNameTranslator(net);
		return translator.get(pc).get(decl);
	}
	
	@Synthesized
	public Port translatePort(Port port) {
		Port originalPort = (Port) get().identifierOwner(port.getIdentifier());
		String portName = get().uniquePortName(originalPort);
		return port.copy(portName);
	}
}
