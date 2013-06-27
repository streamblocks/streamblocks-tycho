package net.opendf.interp.preprocess;

import java.util.BitSet;
import java.util.List;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Scope.ScopeKind;
import net.opendf.ir.am.util.ActorMachineUtils;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;

public class SetScopeInitializers {

	private BitSet getTransientVars(ActorMachine actorMachine) {
		BitSet vars = new BitSet();
		for (Scope s : actorMachine.getScopes()) {
			if (s.getKind() == ScopeKind.Transient) {
				addVars(s.getDeclarations(), vars);
			}
		}
		return vars;
	}

	private void addVars(List<Decl> decls, BitSet bitSet) {
		for (Decl d : decls) {
			if (d instanceof DeclVar) {
				bitSet.set(((DeclVar) d).getVariablePosition());
			}
		}
	}

	private BitSet getVars(List<Scope> scope) {
		BitSet bitSet = new BitSet();
		for (Scope s : scope) {
			addVars(s.getDeclarations(), bitSet);
		}
		return bitSet;
	}

	public void setScopeInitializers(ActorMachine actorMachine) {
		BitSet trans = getTransientVars(actorMachine);
		for (Transition t : ActorMachineUtils.collectTransitions(actorMachine)) {
			t.setRequiredVariables(getVars(t.getScope()));
			t.setInvalidatedVariables(trans);
		}
		for (PredicateCondition c : ActorMachineUtils.collectPredicateConditions(actorMachine)) {
			c.setRequiredVariables(getVars(c.getScope()));
		}
	}

}
