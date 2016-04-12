package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
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
	private static final Lookup lookup = MultiJ.instance(Lookup.class);

	private PortDeclarations() {}

	public static Optional<PortDecl> getDeclaration(Tree<Port> port) {
		return port.parent().flatMap(parent ->
				lookup.lookupPort(parent, parent.node())
		);
	}

	private static Optional<PortDecl> lookupInputPort(Tree<?> context, Port port) {
		return context.findParentOfType(Entity.class)
				.flatMap(entity -> entity.node().getInputPorts().stream()
						.filter(p -> p.getName().equals(port.getName())).findFirst());
	}

	private static Optional<PortDecl> lookupOutputPort(Tree<?> context, Port port) {
		return context.findParentOfType(Entity.class)
				.flatMap(entity -> entity.node().getOutputPorts().stream()
						.filter(p -> p.getName().equals(port.getName())).findFirst());
	}

	@Module
	interface Lookup {
		Optional<PortDecl> lookupPort(Tree context, IRNode node);
		default Optional<PortDecl> lookupPort(Tree context, PortCondition condition) {
			if (condition.isInputCondition()) {
				return lookupInputPort(context, condition.getPortName());
			} else {
				return lookupOutputPort(context, condition.getPortName());
			}
		}

		default Optional<PortDecl> lookupPort(Tree context, InputPattern inputPattern) {
			return lookupInputPort(context, inputPattern.getPort());
		}

		default Optional<PortDecl> lookupPort(Tree context, OutputExpression outputExpression) {
			return lookupOutputPort(context, outputExpression.getPort());
		}

		default Optional<PortDecl> lookupPort(Tree context, ExprInput input) {
			return lookupInputPort(context, input.getPort());
		}

		default Optional<PortDecl> lookupPort(Tree context, StmtConsume consume) {
			return lookupInputPort(context, consume.getPort());
		}

		default Optional<PortDecl> lookupPort(Tree context, StmtRead read) {
			return lookupInputPort(context, read.getPort());
		}

		default Optional<PortDecl> lookupPort(Tree context, StmtWrite write) {
			return lookupOutputPort(context, write.getPort());
		}
	}
}
