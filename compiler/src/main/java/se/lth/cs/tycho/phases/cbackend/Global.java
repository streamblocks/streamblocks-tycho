package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.CallableType;
import se.lth.cs.tycho.types.LambdaType;
import se.lth.cs.tycho.types.ProcType;
import se.lth.cs.tycho.types.Type;

import java.util.List;
import java.util.stream.Collectors;

@Module
public interface Global {
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

	default DefaultValues defVal() { return backend().defaultValues(); }

	void globalCallable(VarDecl decl, Expression def);

	default void globalCallable(VarDecl decl, ExprLambda lambda) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ")
				.append(decl.getName())
				.append("(");
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
			code().assign(type.getReturnType(), "result", lambda.getBody());
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
	}

	default void globalCallable(VarDecl decl, ExprProc proc) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ")
				.append(decl.getName())
				.append("(")
				.append(proc.getValueParameters().stream()
						.map(par -> code().type(types().declaredType(par)) + " " + par.getName())
						.collect(Collectors.joining(", ")));
		String header = builder.toString();
		if (proc.getBody() == null) {
			emitter().emit("%s;", header);
		} else {
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			code().execute(proc.getBody());
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
	}

	default void globalVariables(List<VarDecl> varDecls) {
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (type instanceof CallableType) {
				globalCallable(decl, decl.getValue());
			} else {
				String d = code().declaration(type, decl.getName());
				String v = defVal().defaultValue(type);
				emitter().emit("static %s = %s;", d, v);
			}
		}
		emitter().emit("static void init_global_variables() {");
		emitter().increaseIndentation();
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (type instanceof CallableType) {
				emitter().emit("// function %s", decl.getName());
			} else {
				code().assign(type, decl.getName(), decl.getValue());
			}
		}
		emitter().decreaseIndentation();
		emitter().emit("}");
	}
}
