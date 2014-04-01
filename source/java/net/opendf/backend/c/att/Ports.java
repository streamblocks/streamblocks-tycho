package net.opendf.backend.c.att;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.StmtConsume;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;

public class Ports extends Module<Ports.Required> {

	public interface Required {

		PortDecl lookupPort(IRNode node, Port port);

		PortDecl lookupInputPort(IRNode node, Port port);

		PortDecl lookupOutputPort(IRNode node, Port port);

		PortKind checkPortKind(IRNode node, PortDecl decl);

		Network network(Connection conn);

	}

	@Inherited
	public Network network(Network net) {
		return net;
	}

	@Synthesized
	public PortDecl declaration(Port port) {
		return get().lookupPort(port, port);
	}

	@Inherited
	public PortDecl lookupPort(AbstractIRNode node, Port port) {
		throw new Error();
	}
	
	@Inherited
	public PortDecl lookupPort(Transition t, Port port) {
		for (Port p : t.getInputRates().keySet()) {
			if (p == port) {
				return get().lookupInputPort(t, port);
			}
		}
		for (Port p : t.getOutputRates().keySet()) {
			if (p == port) {
				return get().lookupOutputPort(t, port);
			}
		}
		throw new Error();
	}

	@Inherited
	public PortDecl lookupPort(Connection conn, Port port) {
		Network net = get().network(conn);
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

	@Inherited
	public PortDecl lookupPort(StmtConsume stmt, Port port) {
		return get().lookupInputPort(stmt, port);
	}

	@Inherited
	public PortDecl lookupPort(StmtOutput stmt, Port port) {
		return get().lookupOutputPort(stmt, port);
	}

	@Inherited
	public PortDecl lookupPort(ExprInput expr, Port port) {
		return get().lookupInputPort(expr, port);
	}

	@Inherited
	public PortDecl lookupPort(PortCondition cond, Port port) {
		if (cond.isInputCondition()) {
			return get().lookupInputPort(cond, port);
		} else {
			return get().lookupOutputPort(cond, port);
		}
	}
	
	@Inherited
	public PortDecl lookupInputPort(ActorMachine actorMachine, Port port) {
		for (PortDecl decl : actorMachine.getInputPorts()) {
			if (port.getName().equals(decl.getName())) {
				return decl;
			}
		}
		return null;
	}

	@Inherited
	public PortDecl lookupOutputPort(ActorMachine actorMachine, Port port) {
		for (PortDecl decl : actorMachine.getOutputPorts()) {
			if (port.getName().equals(decl.getName())) {
				return decl;
			}
		}
		return null;
	}
	
	@Synthesized
	public PortKind portKind(PortDecl decl) {
		return get().checkPortKind(decl, decl);
	}
	
	public enum PortKind { INPUT, OUTPUT }
	
	@Inherited
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
	@Inherited
	public PortKind checkPortKind(AbstractIRNode node, PortDecl decl) {
		throw new Error();
	}
	
}
