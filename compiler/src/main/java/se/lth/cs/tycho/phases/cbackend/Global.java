package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.phases.attributes.Types;

import java.util.List;

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

	default void globalVariables(List<VarDecl> varDecls) {
		for (VarDecl decl : varDecls) {
			String d = code().declaration(types().declaredType(decl), decl.getName());
			emitter().emit("static %s;", d);
		}
		emitter().emit("static void init_global_variables() {");
		emitter().increaseIndentation();
		for (VarDecl decl : varDecls) {
			code().assign(types().declaredType(decl), decl.getName(), decl.getValue());
		}
		emitter().decreaseIndentation();
		emitter().emit("}");
	}
}
