package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;

import static org.multij.BindingKind.MODULE;

@Module
public interface Variables {
	@Binding(MODULE)
	Backend backend();

	default String generateTemp() {
		return "t_" + backend().uniqueNumbers().next();
	}

	default String name(Variable var) {
		VarDecl decl = backend().names().declaration(var);
		if (backend().tree().parent(decl) instanceof Scope) {
			return "self->" + var.getName();
		} else if (backend().tree().parent(decl) instanceof ActorMachine) {
			return "self->" + var.getName();
		} else {
			return var.getName();
		}
	}
}
