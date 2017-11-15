package se.lth.cs.tycho.phases.cbackend;

import org.multij.Binding;
import org.multij.Module;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.ClosureVarDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.multij.BindingKind.MODULE;

@Module
public interface Variables {
	@Binding(MODULE)
	Backend backend();

	default String generateTemp() {
		return "t_" + backend().uniqueNumbers().next();
	}

	default String declarationName(VarDecl decl) {
		IRNode parent = backend().tree().parent(decl);
		if (parent instanceof Scope) {
			return "a_" + decl.getName();
		} else if (parent instanceof ActorMachine) {
			return "a_" + decl.getName();
		} else if (parent instanceof NamespaceDecl) {
			QID ns = ((NamespaceDecl) parent).getQID();
			return Stream.concat(ns.parts().stream(), Stream.of(decl.getName()))
					.collect(Collectors.joining("_", "g_", ""));
		} else {
			return "l_" + decl.getName();
		}
	}

	default String globalName(ExprGlobalVariable var) {
		return var.getGlobalName().parts().stream()
				.collect(Collectors.joining("_", "g_", ""));
	}

	default String reference(VarDecl decl) {
		IRNode parent = backend().tree().parent(decl);
		if (parent instanceof Scope || parent instanceof ActorMachine) {
			return "&(self->" + declarationName(decl) + ")";
		} else {
			return "&" + declarationName(decl);
		}
	}

	default String name(Variable var) {
		VarDecl decl = backend().varDecls().declaration(var);
		IRNode parent = backend().tree().parent(decl);
		if (decl instanceof ClosureVarDecl) {
			return "(env->" + declarationName(decl) + ")";
		} else if (parent instanceof Scope || parent instanceof ActorMachine) {
			return "(self->" + declarationName(decl) + ")";
		} else {
			return declarationName(decl);
		}
	}
}
