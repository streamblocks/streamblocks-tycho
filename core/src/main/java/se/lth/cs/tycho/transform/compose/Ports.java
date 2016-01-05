package se.lth.cs.tycho.transform.compose;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class Ports extends Module<Ports.Decls> {

	public interface Decls {

		@Synthesized
		PortDecl declaration(Port port);

		@Inherited
		PortDecl lookupPort(IRNode node, Port port);

		@Inherited
		PortDecl lookupInputPort(IRNode node, Port port);

		@Inherited
		PortDecl lookupOutputPort(IRNode node, Port port);

		@Synthesized
		PortKind portKind(PortDecl decl);

		@Inherited
		PortKind checkPortKind(IRNode node, PortDecl decl);

		@Inherited
		Network network(Connection conn);

	}

	public Network network(Network net) {
		return net;
	}

	public PortDecl declaration(Port port) {
		return e().lookupPort(port, port);
	}

	public PortDecl lookupPort(IRNode node, Port port) {
		throw new Error();
	}

	public PortDecl lookupPort(Transition t, Port port) {
		for (Port p : t.getInputRates().keySet()) {
			if (p == port) {
				return e().lookupInputPort(t, port);
			}
		}
		for (Port p : t.getOutputRates().keySet()) {
			if (p == port) {
				return e().lookupOutputPort(t, port);
			}
		}
		throw new Error();
	}

	public PortDecl lookupPort(Connection conn, Port port) {
		Network net = e().network(conn);
		if (conn.getSrcPort() == port) {
			if (conn.getSrcNodeId() == null) {
				for (PortDecl decl : net.getInputPorts()) {
					if (decl.getName().equals(port.getName())) {
						return decl;
					}
				}
				return null;
			} else {
				PortContainer n = net.getNode(conn.getSrcNodeId()).getContent();
				for (PortDecl decl : n.getOutputPorts()) {
					if (decl.getName().equals(port.getName())) {
						return decl;
					}
				}
				return null;
			}
		} else if (conn.getDstPort() == port) {
			if (conn.getDstNodeId() == null) {
				for (PortDecl decl : net.getOutputPorts()) {
					if (decl.getName().equals(port.getName())) {
						return decl;
					}
				}
				return null;
			} else {
				PortContainer n = net.getNode(conn.getDstNodeId()).getContent();
				for (PortDecl decl : n.getInputPorts()) {
					if (decl.getName().equals(port.getName())) {
						return decl;
					}
				}
				return null;
			}
		}
		throw new Error();
	}

	public PortDecl lookupPort(StmtConsume stmt, Port port) {
		return e().lookupInputPort(stmt, port);
	}

	public PortDecl lookupPort(StmtOutput stmt, Port port) {
		return e().lookupOutputPort(stmt, port);
	}

	public PortDecl lookupPort(ExprInput expr, Port port) {
		return e().lookupInputPort(expr, port);
	}

	public PortDecl lookupPort(PortCondition cond, Port port) {
		if (cond.isInputCondition()) {
			return e().lookupInputPort(cond, port);
		} else {
			return e().lookupOutputPort(cond, port);
		}
	}

	public PortDecl lookupInputPort(ActorMachine actorMachine, Port port) {
		for (PortDecl decl : actorMachine.getInputPorts()) {
			if (port.getName().equals(decl.getName())) {
				return decl;
			}
		}
		return null;
	}

	public PortDecl lookupOutputPort(ActorMachine actorMachine, Port port) {
		for (PortDecl decl : actorMachine.getOutputPorts()) {
			if (port.getName().equals(decl.getName())) {
				return decl;
			}
		}
		return null;
	}

	public PortKind portKind(PortDecl decl) {
		return e().checkPortKind(decl, decl);
	}

	public enum PortKind {
		INPUT, OUTPUT
	}

	public PortKind checkPortKind(ActorMachine actorMachine, PortDecl decl) {
		if (decl == null) {
			return null;
		}
		for (PortDecl other : actorMachine.getInputPorts()) {
			if (decl == other) {
				return PortKind.INPUT;
			}
		}
		for (PortDecl other : actorMachine.getOutputPorts()) {
			if (decl == other) {
				return PortKind.OUTPUT;
			}
		}
		return null;
	}

	public PortKind checkPortKind(AbstractIRNode node, PortDecl decl) {
		throw new Error();
	}

}
