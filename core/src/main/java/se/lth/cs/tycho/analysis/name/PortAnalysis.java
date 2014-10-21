package se.lth.cs.tycho.analysis.name;

import java.util.List;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtOutput;

public class PortAnalysis extends Module<PortAnalysis.Declarations> {

	public interface Declarations {
		@Synthesized
		public PortDecl portDeclaration(Port port);

		@Inherited
		public PortDecl lookupPort(IRNode node, Port port);

		@Inherited
		public PortDecl lookupInputPort(IRNode node, Port port);

		@Inherited
		public PortDecl lookupOutputPort(IRNode node, Port port);
		
		@Inherited
		public Network network(Connection conn);

	}
	
	public Network network(Network network) {
		return network;
	}

	public PortDecl portDeclaration(Port port) {
		return e().lookupPort(port, port);
	}

	public PortDecl lookupPort(InputPattern in, Port port) {
		return e().lookupInputPort(in, port);
	}

	public PortDecl lookupPort(OutputExpression out, Port port) {
		return e().lookupOutputPort(out, port);
	}

	public PortDecl lookupPort(ExprInput in, Port port) {
		return e().lookupInputPort(in, port);
	}

	public PortDecl lookupPort(StmtConsume in, Port port) {
		return e().lookupInputPort(in, port);
	}

	public PortDecl lookupPort(StmtOutput out, Port port) {
		return e().lookupOutputPort(out, port);
	}
	
	public PortDecl lookupPort(PortCondition cond, Port port) {
		return cond.isInputCondition() ? e().lookupInputPort(cond, port) : e().lookupOutputPort(cond, port);
	}
	
	public PortDecl lookupPort(PortContainer cont, Port port) {
		throw new RuntimeException();
	}
	
	public PortDecl lookupPort(Connection conn, Port port) {
		List<PortDecl> decls;
		Network net = e().network(conn);
		if (port == conn.getSrcPort()) {
			if (conn.getSrcNodeId() == null) {
				decls = net.getInputPorts();
			} else {
				Node n = net.getNode(conn.getSrcNodeId());
				decls = n.getContent().getOutputPorts();
			}
		} else if (port == conn.getDstPort()) {
			if (conn.getDstNodeId() == null) {
				decls = net.getOutputPorts();
			} else {
				Node n = net.getNode(conn.getDstNodeId());
				decls = n.getContent().getInputPorts();
			}
		} else {
			return null;
		}
		for (PortDecl decl : decls) {
			if (decl.getName().equals(port.getName())) {
				return decl;
			}
		}
		return null;
	}

	public PortDecl lookupInputPort(PortContainer cont, Port port) {
		for (PortDecl in : cont.getInputPorts()) {
			if (in.getName().equals(port.getName())) {
				return in;
			}
		}
		return null;
	}

	public PortDecl lookupOutputPort(PortContainer cont, Port port) {
		for (PortDecl out : cont.getOutputPorts()) {
			if (out.getName().equals(port.getName())) {
				return out;
			}
		}
		return null;
	}

}
