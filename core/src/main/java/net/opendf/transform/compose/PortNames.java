package net.opendf.transform.compose;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javarag.Cached;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.Port;
import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.entity.PortContainer;
import net.opendf.ir.entity.PortDecl;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

public class PortNames extends Module<PortNames.Decls> {

	public interface Decls {

		PortDecl declaration(Port port);

		@Synthesized
		String uniquePortName(PortDecl decl);

		@Synthesized
		String uniquePortName(Port port);

		@Cached
		@Synthesized
		Map<PortContainer, Map<PortDecl, String>> portNameTranslator(Network net);

		@Inherited
		String lookupUniquePortName(IRNode node, PortContainer container, PortDecl decl);

		@Inherited
		PortContainer lookupPortContainer(PortDecl decl);

		@Inherited
		Object identifierOwner(Identifier identifier);

		@Synthesized
		public Port translatePort(Port port);
	}

	public Object identifierOwner(Object owner) {
		return owner;
	}

	public String uniquePortName(Port port) {
		PortDecl decl = e().declaration(port);
		return e().uniquePortName(decl);
	}

	public String uniquePortName(PortDecl decl) {
		return e().lookupUniquePortName(decl, e().lookupPortContainer(decl), decl);
	}

	public PortContainer lookupPortContainer(ActorMachine am) {
		return am;
	}

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

	public String lookupUniquePortName(Network net, PortContainer pc, PortDecl decl) {
		Map<PortContainer, Map<PortDecl, String>> translator = e().portNameTranslator(net);
		return translator.get(pc).get(decl);
	}

	public Port translatePort(Port port) {
		Port originalPort = (Port) e().identifierOwner(port.getIdentifier());
		String portName = e().uniquePortName(originalPort);
		return port.copy(portName);
	}
}
