package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.CallableType;
import se.lth.cs.tycho.types.LambdaType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	default Names names() { return backend().names(); }

	default DefaultValues defVal() { return backend().defaultValues(); }


	default void actorDecl(EntityDecl decl) {
		actor(decl.getName(), decl.getEntity());
	}

	default void actor(String name, Entity entity) {}

	default void actor(String name, ActorMachine actorMachine) {
		actorMachineState(name, actorMachine);
		actorMachineCallables(name, actorMachine);
		actorMachineStateInit(name, actorMachine);
		actorMachineInit(name, actorMachine);
		actorMachineTransitions(name, actorMachine);
		actorMachineConditions(name, actorMachine);
		actorMachineController(name, actorMachine);
	}

	default void actorMachineController(String name, ActorMachine actorMachine) {
		backend().controllers().emitController(name, actorMachine);
		emitter().emit("");
		emitter().emit("");
	}

	default void actorMachineInit(String name, ActorMachine actorMachine) {
		String selfParameter = name + "_state *self";
		List<String> parameters = getEntityInitParameters(selfParameter, actorMachine);
		emitter().emit("static void %s_init_actor(%s) {", name, String.join(", ", parameters));
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

	default List<String> getEntityInitParameters(String selfParameter, Entity actorMachine) {
		List<String> parameters = new ArrayList<>();
		parameters.add(selfParameter);
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
		return parameters;
	}

	default void actorMachineTransitions(String name, ActorMachine actorMachine) {
		int i = 0;
		for (Transition transition : actorMachine.getTransitions()) {
			emitter().emit("static void %s_transition_%d(%s_state *self) {", name, i, name);
			emitter().increaseIndentation();
			transition.getBody().forEach(code()::execute);
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
			return String.format("fifo_tokens(self->%s_channel, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());

			//return String.format("channel_has_data(self->%s_channel, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());
		} else {
			return String.format("fifo_space(self->%s_channels, self->%1$s_count, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());

			//return String.format("channel_has_space(self->%s_channels, self->%1$s_count, sizeof(%s) * %d)", condition.getPortName().getName(), code().type(types().portType(condition.getPortName())), condition.N());
		}
	}


	default void actorMachineStateInit(String name, ActorMachine actorMachine) {
		int i = 0;
		for (Scope scope : actorMachine.getScopes()) {
			emitter().emit("static void %s_init_scope_%d(%s_state *self) {", name, i, name);
			emitter().increaseIndentation();
			for (VarDecl var : scope.getDeclarations()) {
				if (types().declaredType(var) instanceof CallableType) {
					emitter().emit("// function %s", var.getName());
				} else if (var.getValue() != null) {
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

	default void actorMachineCallables(String name, ActorMachine actorMachine) {
		for (Scope scope : actorMachine.getScopes()) {
			for (VarDecl decl : scope.getDeclarations()) {
				if (types().declaredType(decl) instanceof CallableType) {
					if (scope.isPersistent() && decl.isConstant()) {
						actorMachineCallable(name, decl, decl.getValue());
					} else {
						throw new UnsupportedOperationException();
					}
				}
			}
		}
	}

	void actorMachineCallable(String name, VarDecl decl, Expression value);
	default void actorMachineCallable(String name, VarDecl decl, ExprLambda lambda) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ")
				.append(decl.getName())
				.append("(")
				.append(name).append("_state *self, ");
		for (VarDecl par : lambda.getValueParameters()) {
			builder.append(code().type(types().declaredType(par)))
					.append(" ")
					.append(par.getName())
					.append(", ");
		}
		LambdaType type = (LambdaType) types().declaredType(decl);
		builder.append(code().type(type.getReturnType()))
				.append(" *result)");
		String header = builder.toString();
		if (lambda.getBody() == null) {
			emitter().emit("%s;", header);
		} else {
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			code().assign(type.getReturnType(), "*result", lambda.getBody());
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
	}

	default void actorMachineCallable(String name, VarDecl decl, ExprProc proc) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ")
				.append(decl.getName())
				.append("(")
				.append(name).append("_state *self")
				.append(proc.getValueParameters().stream()
						.map(par -> ", " + code().type(types().declaredType(par)) + " " + par.getName())
						.collect(Collectors.joining()))
				.append(")");
		String header = builder.toString();
		if (proc.getBody() == null) {
			emitter().emit("%s;", header);
		} else {
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			proc.getBody().forEach(code()::execute);
			emitter().decreaseIndentation();
			emitter().emit("}");
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
				if (types().declaredType(var) instanceof CallableType) {
					emitter().emit("// function %s", var.getName());
				} else {
					String decl = code().declaration(types().declaredType(var), var.getName());
					emitter().emit("%s;", decl);
				}
			}
			emitter().emit("");
			i++;
		}
		emitter().decreaseIndentation();
		emitter().emit("} %s_state;", name);
		emitter().emit("");
		emitter().emit("");
	}

	default void actorDecls(List<EntityDecl> entityDecls) {
		entityDecls.forEach(backend().structure()::actorDecl);
	}
}
