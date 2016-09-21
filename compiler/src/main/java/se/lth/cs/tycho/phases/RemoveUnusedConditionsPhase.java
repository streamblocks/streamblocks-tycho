package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.Transformations;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.BitSet;

public class RemoveUnusedConditionsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Removes actor machine conditions that are not used.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return Transformations.transformEntityDecls(task, this::removeUnusedConditions);
	}

	private GlobalEntityDecl removeUnusedConditions(GlobalEntityDecl entityDecl) {
		if (entityDecl.getEntity() instanceof ActorMachine) {
			ActorMachine actorMachine = (ActorMachine) entityDecl.getEntity();
			BitSet used = actorMachine.controller().getStateList().stream()
					.flatMap(state -> state.getInstructions().stream())
					.filter(instruction -> instruction instanceof Test)
					.mapToInt(instruction -> ((Test) instruction).condition())
					.collect(BitSet::new, BitSet::set, BitSet::or);
			ImmutableList.Builder<Condition> conditions = ImmutableList.builder();
			for (int i = 0; i < actorMachine.getConditions().size(); i++) {
				if (used.get(i)) {
					conditions.add(actorMachine.getConditions().get(i));
				} else {
					conditions.add(new PredicateCondition(new ExprLiteral(ExprLiteral.Kind.False)));
				}
			}
			return entityDecl.withEntity(actorMachine.withConditions(conditions.build()));
		} else {
			return entityDecl;
		}
	}
}
