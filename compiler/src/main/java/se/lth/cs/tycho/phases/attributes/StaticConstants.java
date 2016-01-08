package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.decl.LocationKind;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.function.Predicate;

import static se.lth.cs.tycho.phases.attributes.util.Attributes.circular;
import static se.lth.cs.tycho.phases.attributes.util.Attributes.predicate;

@Module
public interface StaticConstants {

	ModuleKey<StaticConstants> key = (task, manager) -> MultiJ.from(StaticConstants.class)
			.bind("names").to(manager.getAttributeModule(Names.key, task))
			.bind("closures").to(manager.getAttributeModule(Closures.key, task))
			.instance();

	@Binding(BindingKind.INJECTED)
	Names names();

	@Binding(BindingKind.INJECTED)
	Closures closures();

	default Predicate<VarDecl> isConstant() {
		return predicate(circular(false, (VarDecl decl) -> decl.isConstant()
				&& decl.getLocationKind() != LocationKind.PARAMETER
				&& decl.getValue() != null
				&& isConstantExpr(decl.getValue())));
	}

	default boolean isConstantExpr(Expression expr) {
		return closures().freeVariables(expr).stream().allMatch(isConstant())
				&& closures().freePortReferences(expr).isEmpty();
	}


}
