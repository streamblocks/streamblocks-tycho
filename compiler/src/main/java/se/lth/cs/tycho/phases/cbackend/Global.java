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


	default void globalVariables(List<VarDecl> varDecls) {
//		for (VarDecl decl : varDecls) {
//			Type type = types().declaredType(decl);
//			if (type instanceof CallableType) {
//				globalCallableDecl(decl, decl.getValue());
//			}
//		}
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			String d = code().declaration(type, backend().variables().declarationName(decl));
			String v = defVal().defaultValue(type);
			emitter().emit("static %s = %s;", d, v);
		}
//		for (VarDecl decl : varDecls) {
//			Type type = types().declaredType(decl);
//			if (type instanceof CallableType) {
//				globalCallable(decl, decl.getValue());
//			}
//		}
		emitter().emit("static void init_global_variables() {");
		emitter().increaseIndentation();
		for (VarDecl decl : varDecls) {
			Type type = types().declaredType(decl);
			if (decl.isExternal() && type instanceof CallableType) {
				String wrapperName = backend().callables().externalWrapperFunctionName(decl);
				String variableName = backend().variables().declarationName(decl);
				String t = backend().callables().mangle(type).encode();
				emitter().emit("%s = (%s) { *%s, NULL };", variableName, t, wrapperName);
			} else {
				code().assign(type, backend().variables().declarationName(decl), decl.getValue());
			}
		}
		emitter().decreaseIndentation();
		emitter().emit("}");
	}
}
