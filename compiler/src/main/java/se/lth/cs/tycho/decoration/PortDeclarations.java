package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;

import java.util.Optional;

public final class PortDeclarations {

	private PortDeclarations() {}

	public static Optional<Tree<PortDecl>> getDeclaration(Tree<Port> port) {
		if (isInputPort(port)) {
			return lookupInputPort(port);
		} else {
			return lookupOutputPort(port);
		}
	}

	private static boolean isInputPort(Tree<Port> port) {
		Optional<Tree<? extends IRNode>> parentTree = port.parent();
		if (parentTree.isPresent()) {
			IRNode parent = parentTree.get().node();
			if (parent instanceof PortCondition) {
				return ((PortCondition) parent).isInputCondition();
			} else if (parent instanceof InputPattern) {
				return true;
			} else if (parent instanceof ExprInput) {
				return true;
			} else if (parent instanceof StmtConsume) {
				return true;
			} else if (parent instanceof StmtRead) {
				return true;
			} else if (parent instanceof OutputExpression) {
				return false;
			} else if (parent instanceof StmtWrite) {
				return false;
			} else {
				throw new RuntimeException("Port in unknown context.");
			}
		} else {
			throw new IllegalArgumentException("Port has no context.");
		}
	}

	private static Optional<Tree<PortDecl>> lookupInputPort(Tree<Port> port) {
		return port.findParentOfType(Entity.class)
				.flatMap(entity -> entity.children(Entity::getInputPorts)
						.filter(p -> p.node().getName().equals(port.node().getName())).findFirst());
	}

	private static Optional<Tree<PortDecl>> lookupOutputPort(Tree<Port> port) {
		return port.findParentOfType(Entity.class)
				.flatMap(entity -> entity.children(Entity::getOutputPorts)
						.filter(p -> p.node().getName().equals(port.node().getName())).findFirst());
	}
}
