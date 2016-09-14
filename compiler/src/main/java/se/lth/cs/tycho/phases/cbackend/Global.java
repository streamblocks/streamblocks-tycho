package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.types.CallableType;
import se.lth.cs.tycho.types.LambdaType;
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

	void globalCallableDecl(VarDecl decl, Expression def);

	default void globalCallableDecl(VarDecl decl, ExprProc proc) {
		String modifier = proc.isExternal() ? "extern" : "static";
		emitter().emit("%s %s;", modifier, globalCallableHeader(decl, proc, proc.isExternal()));
	}
	default void globalCallableDecl(VarDecl decl, ExprLambda lambda) {
		String modifier = lambda.isExternal() ? "extern" : "static";
		emitter().emit("%s %s;", modifier, globalCallableHeader(decl, lambda, lambda.isExternal()));
	}

	void globalCallable(VarDecl decl, Expression def);

	default void globalCallable(VarDecl decl, ExprLambda lambda) {
		if (!lambda.isExternal()) {
			String header = globalCallableHeader(decl, lambda, false);
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			LambdaType type = (LambdaType) types().declaredType(decl);
			emitter().emit("return %s;", code().evaluate(lambda.getBody()));
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			emitter().emit("extern %s;", globalCallableHeader(decl, lambda, true));
//			String header = globalCallableHeader(decl, lambda, false);
//			emitter().emit("static %s {", header);
//			emitter().increaseIndentation();
//			emitter().emit("*result = %s(%s);", decl.getOriginalName(),
//				lambda.getValueParameters().stream()
//						.map(VarDecl::getName).collect(Collectors.joining(", ")));
//			emitter().decreaseIndentation();
//			emitter().emit("}");
		}
	}

	default void globalCallable(VarDecl decl, ExprProc proc) {
		if (!proc.isExternal()) {
			String header = globalCallableHeader(decl, proc, false);
			emitter().emit("static %s {", header);
			emitter().increaseIndentation();
			proc.getBody().forEach(code()::execute);
			emitter().decreaseIndentation();
			emitter().emit("}");
		} else {
			emitter().emit("extern %s;", globalCallableHeader(decl, proc, true));
//			String header = globalCallableHeader(decl, proc, false);
//			emitter().emit("static %s {", header);
//			emitter().increaseIndentation();
//			emitter().emit("%s(%s);", decl.getOriginalName(), proc.getValueParameters().stream()
//			.map(VarDecl::getName).collect(Collectors.joining(", ")));
//			emitter().decreaseIndentation();
//			emitter().emit("}");
		}
	}

	default String globalCallableHeader(VarDecl decl, ExprProc proc, boolean external) {
		StringBuilder builder = new StringBuilder();
		builder.append("void ")
				.append(external ? decl.getOriginalName() : backend().variables().declarationName(decl))
				.append("(")
				.append(proc.getValueParameters().stream()
						.map(par -> code().declaration(types().declaredType(par), backend().variables().declarationName(par)))
						.collect(Collectors.joining(", ")));
		builder.append(")");
		return builder.toString();
	}

	default String globalCallableHeader(VarDecl decl, ExprLambda lambda, boolean external) {
		if (external) {
			StringBuilder builder = new StringBuilder();
			LambdaType type = (LambdaType) types().declaredType(decl);
			builder.append(code().type(type.getReturnType()))
					.append(" ")
					.append(decl.getOriginalName())
					.append("(")
					.append(lambda.getValueParameters().stream()
							.map(par -> code().declaration(types().declaredType(par), backend().variables().declarationName(par)))
							.collect(Collectors.joining(", ")))
					.append(")");
			return builder.toString();
		} else {
			StringBuilder builder = new StringBuilder();
			LambdaType type = (LambdaType) types().declaredType(decl);
			builder.append(code().type(type.getReturnType()))
					.append(" ")
					.append(backend().variables().declarationName(decl))
					.append("(");
			boolean first = true;
			for (VarDecl par : lambda.getValueParameters()) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(code().declaration(types().declaredType(par), backend().variables().declarationName(par)));
			}
			builder.append(")");
			return builder.toString();
		}
	}

	default void globalVariables(List<VarDecl> varDecls) {
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (type instanceof CallableType) {
				globalCallableDecl(decl, decl.getValue());
			}
		}
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (!(type instanceof CallableType)) {
				String d = code().declaration(type, backend().variables().declarationName(decl));
				String v = defVal().defaultValue(type);
				emitter().emit("static %s = %s;", d, v);
			}
		}
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (type instanceof CallableType) {
				globalCallable(decl, decl.getValue());
			}
		}
		emitter().emit("static void init_global_variables() {");
		emitter().increaseIndentation();
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (type instanceof CallableType) {
				emitter().emit("// function %s", decl.getName());
			} else {
				code().assign(type, backend().variables().declarationName(decl), decl.getValue());
			}
		}
		emitter().decreaseIndentation();
		emitter().emit("}");
	}
}
