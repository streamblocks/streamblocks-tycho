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
			emitter().emit("self->%s = %1$s;", backend().variables().declarationName(d));
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
			parameters.add(code().declaration(types().declaredType(d), backend().variables().declarationName(d)));
		});
		actorMachine.getInputPorts().forEach(p -> {
			String type = code().type(types().declaredPortType(p));
			parameters.add(String.format("channel_%s *%s_channel", type, p.getName()));
		});
		actorMachine.getOutputPorts().forEach(p -> {
			String type = code().type(types().declaredPortType(p));
			parameters.add(String.format("channel_%s **%s_channels", type, p.getName()));
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
			return String.format("channel_has_data_%s(self->%s_channel, %d)", code().type(types().portType(condition.getPortName())), condition.getPortName().getName(), condition.N());
		} else {
			return String.format("channel_has_space_%s(self->%s_channels, self->%2$s_count, %d)", code().type(types().portType(condition.getPortName())), condition.getPortName().getName(), condition.N());
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
					code().assign(types().declaredType(var), "self->" + backend().variables().declarationName(var), var.getValue());
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
		LambdaType type = (LambdaType) types().declaredType(decl);
		builder.append(code().type(type.getReturnType()));
		builder.append(" ");
		builder.append(lambda.isExternal() ? decl.getOriginalName() : decl.getName());
		builder.append("(");
		boolean first = true;
		if (!lambda.isExternal()) {
			builder.append(name).append("_state *self");
			first = false;
		}
		for (VarDecl par : lambda.getValueParameters()) {
			if (first) {
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(code().type(types().declaredType(par)))
					.append(" ")
					.append(backend().variables().declarationName(par));
		}
		builder.append(")");
		String header = builder.toString();
		if (lambda.isExternal()) {
			emitter().emit("%s;", header);
		} else {
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			emitter().emit("return %s;", code().evaluate(lambda.getBody()));
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
	}

	default void actorMachineCallable(String name, VarDecl decl, ExprProc proc) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ");
		builder.append(proc.isExternal() ? decl.getOriginalName() : decl.getName());
		builder.append("(");
		if (!proc.isExternal()) {
			builder.append(name).append("_state *self, ");
		}
		builder.append(proc.getValueParameters().stream()
				.map(par -> code().type(types().declaredType(par)) + " " + backend().variables().declarationName(par))
				.collect(Collectors.joining(", ")))
				.append(")");
		String header = builder.toString();
		if (proc.isExternal()) {
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
			String decl = code().declaration(types().declaredType(param), backend().variables().declarationName(param));
			emitter().emit("%s;", decl);
		}
		emitter().emit("");

		emitter().emit("// input ports");
		for (PortDecl input : actorMachine.getInputPorts()) {
			String type = code().type(types().declaredPortType(input));
			emitter().emit("channel_%s *%s_channel;", type, input.getName());
		}
		emitter().emit("");

		emitter().emit("// output ports");
		for (PortDecl output : actorMachine.getOutputPorts()) {
			String type = code().type(types().declaredPortType(output));
			emitter().emit("channel_%s **%s_channels;", type, output.getName());
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
					String decl = code().declaration(types().declaredType(var), backend().variables().declarationName(var));
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
