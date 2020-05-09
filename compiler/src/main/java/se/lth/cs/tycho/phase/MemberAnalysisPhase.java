package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.type.AliasType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.Type;

import java.util.Objects;

import static org.multij.BindingKind.INJECTED;

public class MemberAnalysisPhase implements Phase {

	@Override
	public String getDescription() {
		return "Checks member existence in type definition";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		MemberChecker checker = MultiJ.from(MemberChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("types").to(task.getModule(Types.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.accept(task);
		return task;
	}

	@Module
	interface MemberChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(INJECTED)
		Types types();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void accept(IRNode node) {
			check(node);
			node.forEachChild(this::accept);
		}

		default void check(IRNode node) {

		}

		default void check(ExprField expr) {
			Type type = types().type(expr.getStructure());
			checkMember(expr, type, expr.getField().getName());
		}

		default void check(LValueField lvalue) {
			Type type = types().lvalueType(lvalue.getStructure());
			checkMember(lvalue, type, lvalue.getField().getName());
		}

		default void checkMember(IRNode node, Type type, String member) {
			error(node, type, member);
		}

		default void checkMember(IRNode node, AliasType type, String member) {
			checkMember(node, type.getConcreteType(), member);
		}

		default void checkMember(IRNode node, ProductType type, String member) {
			if (type.getFields().stream().noneMatch(field -> Objects.equals(field.getName(), member))) {
				error(node, type, member);
			}
		}

		default void error(IRNode node, Type type, String member) {
			reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "No member " + member + " in type " + type + ".", sourceUnit(node), node));
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
