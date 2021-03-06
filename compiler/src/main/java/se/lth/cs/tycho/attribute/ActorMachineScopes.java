package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ActorMachineScopes {
	ModuleKey<ActorMachineScopes> key = task -> MultiJ.from(Implementation.class)
			.bind("tree").to(task.getModule(TreeShadow.key))
			.instance();

	BitSet required(ActorMachine actorMachine, Instruction instruction);
	BitSet init(ActorMachine actorMachine, Instruction instruction);
	BitSet persistentScopes(ActorMachine actorMachine);
	BitSet transientScopes(ActorMachine actorMachine);

	@Module
	interface Implementation extends ActorMachineScopes {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.LAZY)
		default Map<ActorMachine, Map<String, Integer>> variableScopes() {
			ScopeVarCollector collector = MultiJ.instance(ScopeVarCollector.class);
			collector.accept(tree().root());
			return collector.variableScopes();
		}

		default Set<Variable> variableUses(IRNode node) {
			VariableUseCollector collector = MultiJ.instance(VariableUseCollector.class);
			collector.accept(node);
			return collector.variables();
		}

		@Module
		interface VariableUseCollector extends Consumer<IRNode> {
			@Binding(BindingKind.LAZY)
			default Set<Variable> variables() {
				return new HashSet<>();
			}

			@Override
			default void accept(IRNode node) {
				node.forEachChild(this);
			}

			default void accept(Variable var) {
				variables().add(var);
			}
		}

		@Module
		interface ScopeVarCollector extends Consumer<IRNode> {
			@Binding(BindingKind.LAZY)
			default Map<ActorMachine, Map<String, Integer>> variableScopes() {
				return new HashMap<>();
			}

			@Override
			default void accept(IRNode node) {
				node.forEachChild(this);
			}

			default void accept(Decl decl) {
			}

			default void accept(GlobalEntityDecl entityDecl) {
				accept(entityDecl.getEntity());
			}

			default void accept(Entity entity) {
			}

			default void accept(ActorMachine actorMachine) {
				int i = 0;
				Map<String, Integer> scopeMap = new HashMap<>();
				variableScopes().put(actorMachine, scopeMap);
				for (Scope s : actorMachine.getScopes()) {
					for (VarDecl d : s.getDeclarations()) {
						scopeMap.put(d.getName(), i);
					}
					i = i + 1;
				}
			}
		}

		@Override
		default BitSet transientScopes(ActorMachine actorMachine) {
			int i = 0;
			BitSet result = new BitSet();
			for (Scope s : actorMachine.getScopes()) {
				if (!s.isPersistent()) {
					result.set(i);
				}
				i = i + 1;
			}
			return result;
		}

		@Override
		default BitSet persistentScopes(ActorMachine actorMachine) {
			BitSet result = new BitSet();
			result.set(0, actorMachine.getScopes().size());
			result.andNot(transientScopes(actorMachine));
			return result;
		}

		@Override
		default BitSet init(ActorMachine actorMachine, Instruction instruction) {
			BitSet result = required(actorMachine, instruction);
			result.andNot(persistentScopes(actorMachine));
			return result;
		}

		@Override
		BitSet required(ActorMachine actorMachine, Instruction instruction);

		default BitSet required(ActorMachine actorMachine, Exec exec) {
			Transition t = actorMachine.getTransitions().get(exec.transition());
			return transitiveReferences(actorMachine, scopeReferences(actorMachine, t));
		}

		default BitSet required(ActorMachine actorMachine, Wait wait) {
			return new BitSet();
		}

		default BitSet required(ActorMachine actorMachine, Test test) {
			Condition c = actorMachine.getConditions().get(test.condition());
			return transitiveReferences(actorMachine, scopeReferences(actorMachine, c));
		}

		default BitSet scopeReferences(ActorMachine actorMachine, IRNode node) {
			return variableUses(node).stream()
					.map(Variable::getName)
					.filter(variableScopes().get(actorMachine)::containsKey)
					.mapToInt(variableScopes().get(actorMachine)::get)
					.collect(BitSet::new, BitSet::set, BitSet::or);
		}

		default BitSet transitiveReferences(ActorMachine actorMachine, BitSet scope) {
			BitSet result = new BitSet();
			result.or(scope);
			BitSet add = scope;
			while (!add.isEmpty()) {
				BitSet added = add.stream()
						.mapToObj(actorMachine.getScopes()::get)
						.map(s -> scopeReferences(actorMachine, s))
						.collect(BitSet::new, BitSet::or, BitSet::or);
				add = new BitSet();
				add.or(added);
				add.andNot(result);
				result.or(added);
			}
			return result;
		}
	}
}
