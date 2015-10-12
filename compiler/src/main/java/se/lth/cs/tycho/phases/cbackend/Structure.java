package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.instance.am.ctrl.Controller;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.instance.am.ctrl.Wait;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.phases.attributes.Types;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Module
public interface Structure {
	@Binding
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Code code() {
		return backend().code();
	}

	default Types types() {
		return backend().types();
	}


	default void entityDecl(EntityDecl decl) {
		entity(decl.getName(), decl.getEntity());
	}

	void entity(String name, Entity entity);

	default void entity(String name, ActorMachine actorMachine) {
		actorMachineState(name, actorMachine);
		actorMachineStateInit(name, actorMachine);
		actorMachineInit(name, actorMachine);
		actorMachineTransitions(name, actorMachine);
		actorMachineConditions(name, actorMachine);
		actorMachineController(name, actorMachine);
	}

	default void actorMachineController(String name, ActorMachine actorMachine) {
		Set<State> waitTargets = getWaitTargets(actorMachine.controller());
	}

	default Set<State> getWaitTargets(Controller controller) {
		Set<State> result = new HashSet<>();
		Set<State> visited = new HashSet<>();
		Queue<State> queue = new ArrayDeque<>();
		queue.add(controller.getInitialState());
		while (!queue.isEmpty()) {
			State s = queue.remove();
			if (visited.add(s)) {
				for (Instruction i : s.getInstructions()) {
					i.forEachTarget(queue::add);
					if (i instanceof Wait) {
						result.add(((Wait) i).target());
					}
				}
			}
		}
		return result;
	}

	default void actorMachineInit(String name, ActorMachine actorMachine) {
		List<String> parameters = new ArrayList<>();
		parameters.add(name + "_state *self");
		actorMachine.getValueParameters().forEach(d -> {
			parameters.add(code().declaration(types().declaredType(d), d.getName()));
		});
		actorMachine.getInputPorts().forEach(p -> {
			parameters.add(String.format("channel_t *%s_channel", p.getName()));
		});
		actorMachine.getOutputPorts().forEach(p -> {
			parameters.add(String.format("channel_t **%s_channels", p.getName()));
			parameters.add(String.format("size_t %s_count", p.getName()));
		});
		emitter().emit("void %s_init_actor(%s) {", name, String.join(", ", parameters));
		emitter().increaseIndentation();
		emitter().emit("self->program_counter = 0;");
		emitter().emit("");

		emitter().emit("// parameters");
		actorMachine.getValueParameters().forEach(d -> {
			emitter().emit("self->%s = %1$s;", d.getName());
		});
		emitter().emit("");

		emitter().emit("// input ports");
		actorMachine.getInputPorts().forEach(p -> {
			emitter().emit("self->%s_channel = %1$s_channel;", p.getName());
		});
		emitter().emit("");

		emitter().emit("// output ports");
		actorMachine.getOutputPorts().forEach(p -> {
			emitter().emit("self->%s_channels = %1$s_channels;", p.getName());
			emitter().emit("self->%s_count = %1$s_count;", p.getName());
		});
		emitter().emit("");

		emitter().emit("// init persistent scopes");
		int i = 0;
		for (Scope s : actorMachine.getScopes()) {
			if (s.isPersistent()) {
				emitter().emit("%s_init_scope_%d(self);", name, i);
			}
			i = i + 1;
		}
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
		emitter().emit("");
	}

	default void actorMachineTransitions(String name, ActorMachine actorMachine) {
		int i = 0;
		for (Transition transition : actorMachine.getTransitions()) {
			emitter().emit("static void %s_transition_%d(%s_state *self) {", name, i, name);
			emitter().increaseIndentation();
			code().execute(transition.getBody());
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
			emitter().emit("");
			i++;
		}
	}

	default void actorMachineConditions(String name, ActorMachine actorMachine) {
		int i = 0;
		for (Condition condition : actorMachine.getConditions()) {
			emitter().emit("static _Bool %s_condition_%d(%s_state *self) {", name, i, name);
			emitter().increaseIndentation();
			emitter().emit("return %s;", evaluateCondition(condition));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
			emitter().emit("");
			i++;
		}
	}

	String evaluateCondition(Condition condition);

	default String evaluateCondition(PredicateCondition condition) {
		return code().evaluate(condition.getExpression());
	}

	default String evaluateCondition(PortCondition condition) {
		if (condition.isInputCondition()) {
			return String.format("channel_has_data(self->%s_channel, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());
		} else {
			return String.format("channel_has_space(self->%s_channels, self->%1$s_count, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());
		}
	}


	default void actorMachineStateInit(String name, ActorMachine actorMachine) {
		int i = 0;
		for (Scope scope : actorMachine.getScopes()) {
			emitter().emit("static void %s_init_scope_%d(%s_state *self) {", name, i, name);
			emitter().increaseIndentation();
			for (VarDecl var : scope.getDeclarations()) {
				if (var.getValue() != null) {
					code().assign(types().declaredType(var), "self->" + var.getName(), var.getValue());
				}
			}
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
			emitter().emit("");
			i++;
		}
	}

	default void actorMachineState(String name, ActorMachine actorMachine) {
		emitter().emit("typedef struct {");
		emitter().increaseIndentation();

		emitter().emit("int program_counter;");
		emitter().emit("");

		emitter().emit("// parameters");
		for (VarDecl param : actorMachine.getValueParameters()) {
			String decl = code().declaration(types().declaredType(param), param.getName());
			emitter().emit("%s;", decl);
		}
		emitter().emit("");

		emitter().emit("// input ports");
		for (PortDecl input : actorMachine.getInputPorts()) {
			emitter().emit("channel_t *%s_channel;", input.getName());
		}
		emitter().emit("");

		emitter().emit("// output ports");
		for (PortDecl output : actorMachine.getOutputPorts()) {
			emitter().emit("channel_t **%s_channels;", output.getName());
			emitter().emit("size_t %s_count;", output.getName());
		}
		emitter().emit("");

		int i = 0;
		for (Scope scope : actorMachine.getScopes()) {
			emitter().emit("// scope %d", i);
			for (VarDecl var : scope.getDeclarations()) {
				String decl = code().declaration(types().declaredType(var), var.getName());
				emitter().emit("%s;", decl);
			}
			emitter().emit("");
			i++;
		}
		emitter().decreaseIndentation();
		emitter().emit("} %s_state;", name);
		emitter().emit("");
		emitter().emit("");
	}
}
