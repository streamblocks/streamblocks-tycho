package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class PortDeclarations {

	private PortDeclarations() {}

	public static Optional<Tree<PortDecl>> getDeclaration(Tree<Port> port) {
		if (isInputPort(port)) {
			return lookupInputPort(port);
		} else {
			return lookupOutputPort(port);
		}
	}

	public static Optional<Tree<PortDecl>> getConnectionEnd(Tree<PortReference> port) {
		Optional<Tree<GlobalEntityDecl>> entity;
		boolean isInputPort;
		if (InstanceDeclarations.isBoundaryPort(port.node())) {
			entity = port.findParentOfType(GlobalEntityDecl.class);
			isInputPort = isSourcePortRef(port);
		} else {
			entity = InstanceDeclarations.getDeclaration(port)
					.map(i -> i.child(InstanceDecl::getEntityExpr))
					.flatMap(i -> i.tryCast(EntityInstanceExpr.class))
					.map(i -> i.child(EntityInstanceExpr::getEntityName))
					.flatMap(EntityDeclarations::getDeclaration);
			isInputPort = !isSourcePortRef(port);
		}
		if (entity.isPresent()) {
			return entity.get().child(GlobalEntityDecl::getEntity)
					.children(isInputPort ? Entity::getInputPorts : Entity::getOutputPorts)
					.filter(p -> p.node().getName().equals(port.node().getPortName()))
					.findFirst();
		} else {
			return Optional.empty();
		}
	}

	private static boolean isSourcePortRef(Tree<PortReference> port) {
		Optional<Tree<StructureConnectionStmt>> conn = port.parent()
				.flatMap(t -> t.tryCast(StructureConnectionStmt.class));
		assert conn.isPresent();
		assert conn.get().node().getSrc() == port.node() || conn.get().node().getDst() == port.node();
		return conn.get().node().getSrc() == port.node();
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
