package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.ExprMember;
import se.lth.cs.tycho.ir.module.ModuleDecl;

public interface ModuleMembers {
	ModuleKey<ModuleMembers> key = task -> MultiJ.from(Implementation.class)
			.bind("modules").to(task.getModule(ModuleDeclarations.key))
			.instance();

	LocalVarDecl valueMember(ExprMember memberExpr);

	@Module
	interface Implementation extends ModuleMembers {
		@Binding(BindingKind.INJECTED)
		ModuleDeclarations modules();

		default LocalVarDecl valueMember(ExprMember memberExpr) {
			ModuleDecl module = modules().declaration(memberExpr.getModule());
			return module.getValueComponents().stream()
					.filter(var -> var.getName().equals(memberExpr.getMember()))
					.findFirst()
					.orElse(null);
		}

	}
}
