package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityIfExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityListExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@Module
public interface NlNetworks {
	ModuleKey<NlNetworks> key = (unit, manager) -> MultiJ.from(NlNetworks.class)
			.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
			.bind("names").to(manager.getAttributeModule(Names.key, unit))
			.bind("constants").to(manager.getAttributeModule(ConstantEvaluator.key, unit))
			.instance();

	@Binding(BindingKind.INJECTED)
	TreeShadow tree();

	@Binding(BindingKind.INJECTED)
	Names names();

	@Binding(BindingKind.INJECTED)
	ConstantEvaluator constants();

	default EntityInstanceExpr sourceInstance(StructureConnectionStmt connection) {
		return enclosingNetwork(connection).getEntities().stream()
				.filter(entry -> entry.getKey().equals(connection.getSrc().getEntityName()))
				.map(entry -> evaluate(entry.getValue(), connection.getSrc().getEntityIndex()))
				.findFirst()
				.get();
	}

	default EntityInstanceExpr targetInstance(StructureConnectionStmt connection) {
		return enclosingNetwork(connection).getEntities().stream()
				.filter(entry -> entry.getKey().equals(connection.getDst().getEntityName()))
				.map(entry -> evaluate(entry.getValue(), connection.getDst().getEntityIndex()))
				.findFirst()
				.get();
	}

	EntityInstanceExpr evaluate(EntityExpr expr, List<Expression> indices);

	default EntityInstanceExpr evaluate(EntityInstanceExpr expr, List<Expression> indices) {
		assert indices.isEmpty() : "Expected no index";
		return expr;
	}

	default EntityInstanceExpr evaluate(EntityListExpr expr, List<Expression> indices) {
		assert !expr.getEntityList().isEmpty() : "Expected index";
		OptionalLong index = constants().intValue(indices.get(0));
		assert index.isPresent() : "Could not determine value";
		return evaluate(expr.getEntityList().get((int) index.getAsLong()), indices.subList(1, indices.size()));
	}

	default EntityInstanceExpr evaluate(EntityIfExpr expr, List<Expression> indices) {
		Optional<Boolean> condition = constants().boolValue(expr.getCondition());
		assert condition.isPresent() : "Condition must be constant";
		if (condition.get()) {
			return evaluate(expr.getTrueEntity(), indices);
		} else {
			return evaluate(expr.getFalseEntity(), indices);
		}
	}

	default NlNetwork enclosingNetwork(IRNode node) {
		IRNode parent = tree().parent(node);
		while (parent != null && !(parent instanceof NlNetwork)) {
			parent = tree().parent(parent);
		}
		return parent == null ? null : (NlNetwork) parent;
	}
}
