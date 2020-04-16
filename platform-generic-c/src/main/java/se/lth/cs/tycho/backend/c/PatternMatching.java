package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeclaration;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.SumType;
import se.lth.cs.tycho.type.Type;

import java.util.Objects;

@Module
public interface PatternMatching {
	@Binding(BindingKind.INJECTED)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Code code() {
		return backend().code();
	}

	default String evaluate(ExprCase caseExpr) {
		backend().memoryStack().enterScope();
		String expr = code().variables().generateTemp();
		emitter().emit("%s = %s;", code().declaration(code().types().type(caseExpr.getExpression()), expr), code().evaluate(caseExpr.getExpression()));
		String match = code().variables().generateTemp();
		emitter().emit("%s = false;", code().declaration(BoolType.INSTANCE, match));
		Type type = code().types().type(caseExpr);
		String result = code().variables().generateTemp();
		if ((type instanceof AlgebraicType) || (code().isAlgebraicTypeList(type))) {
			backend().memoryStack().trackPointer(result, type);
		}
		emitter().emit("%s = %s;", code().declaration(type, result), backend().defaultValues().defaultValue(type));
		caseExpr.getAlternatives().forEach(alternative -> {
			emitter().emit("if (!%s) {", match);
			backend().memoryStack().enterScope();
			emitter().increaseIndentation();
			evaluateAlternative(alternative, expr, result, match);
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		emitter().emit("if (!%s) {", match);
		emitter().increaseIndentation();
		backend().memoryStack().enterScope();
		code().copy(code().types().type(caseExpr), result, code().types().type(caseExpr.getDefault()), code().evaluate(caseExpr.getDefault()));
		backend().memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
		backend().memoryStack().exitScope();
		return result;
	}

	default void execute(StmtCase caseStmt) {
		String expr = code().variables().generateTemp();
		emitter().emit("%s = %s;", code().declaration(code().types().type(caseStmt.getExpression()), expr), code().evaluate(caseStmt.getExpression()));
		String match = code().variables().generateTemp();
		emitter().emit("%s = false;", code().declaration(BoolType.INSTANCE, match));
		caseStmt.getAlternatives().forEach(alternative -> {
			emitter().emit("if (!%s) {", match);
			emitter().increaseIndentation();
			backend().memoryStack().enterScope();
			executeAlternative(alternative, expr, match);
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		caseStmt.getDefault().ifPresent(default_ -> {
			emitter().emit("if (!%s) {", match);
			emitter().increaseIndentation();
			backend().memoryStack().enterScope();
			code().execute(default_);
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
	}

	default void evaluateAlternative(ExprCase.Alternative alternative, String expr, String result, String match) {
		openPattern(alternative.getPattern(), expr, "", "");
		alternative.getGuards().forEach(guard -> {
			emitter().emit("if (%s) {", code().evaluate(guard));
			emitter().increaseIndentation();
			backend().memoryStack().enterScope();
		});
		code().copy(code().types().type(alternative.getExpression()), result, code().types().type(alternative.getExpression()), code().evaluate(alternative.getExpression()));
		emitter().emit("%s = true;", match);
		alternative.getGuards().forEach(guard -> {
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		closePattern(alternative.getPattern());
	}

	default void executeAlternative(StmtCase.Alternative alternative, String expr, String match) {
		openPattern(alternative.getPattern(), expr, "", "");
		alternative.getGuards().forEach(guard -> {
			emitter().emit("if (%s) {", code().evaluate(guard));
			emitter().increaseIndentation();
			backend().memoryStack().enterScope();
		});
		alternative.getStatements().forEach(statement -> {
			code().execute(statement);
		});
		emitter().emit("%s = true;", match);
		alternative.getGuards().forEach(guard -> {
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		closePattern(alternative.getPattern());
	}

	void openPattern(Pattern pattern, String target, String deref, String member);

	default void openPattern(PatternDeconstruction pattern, String target, String deref, String member) {
		Type type = code().types().type(pattern);
		if (type instanceof SumType) {
			SumType sum = (SumType) type;
			SumType.VariantType variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), pattern.getName())).findAny().get();
			emitter().emit("if (%s->tag == tag_%s_%s) {", member == "" ? target : target + deref + member, sum.getName(), variant.getName());
			emitter().increaseIndentation();
			backend().memoryStack().enterScope();
			for (int i = 0; i < variant.getFields().size(); ++i) {
				openPattern(pattern.getPatterns().get(i), (member == "" ? target : target + deref + member) + "->data." + pattern.getName(), ".", variant.getFields().get(i).getName());
			}
		} else if (type instanceof ProductType) {
			ProductType product = (ProductType) type;
			for (int i = 0; i < product.getFields().size(); ++i) {
				openPattern(pattern.getPatterns().get(i), member == "" ? target : target + deref + member, "->", product.getFields().get(i).getName());
			}
		}
	}

	default void openPattern(PatternExpression pattern, String target, String deref, String member) {
		Type type = backend().types().type(pattern.getExpression());
		emitter().emit("if (%s) {", code().compare(type, String.format("%s%s%s", target, deref, member), type, code().evaluate(pattern.getExpression())));
		emitter().increaseIndentation();
		backend().memoryStack().enterScope();
	}

	default void openPattern(PatternBinding pattern, String target, String deref, String member) {
		emitter().emit("%s = %s%s%s;", code().declaration(code().types().type(pattern), backend().variables().declarationName(pattern.getDeclaration())), target, deref, member);
	}

	default void openPattern(PatternVariable pattern, String target, String deref, String member) {
		emitter().emit("%s = %s%s%s;", backend().variables().name(pattern.getVariable()), target, deref, member);
	}

	default void openPattern(PatternWildcard pattern, String target, String deref, String member) {

	}

	void closePattern(Pattern pattern);

	default void closePattern(PatternDeconstruction pattern) {
		Type type = code().types().type(pattern);
		if (type instanceof SumType) {
			backend().memoryStack().exitScope();
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
		pattern.getPatterns().forEach(this::closePattern);
	}

	default void closePattern(PatternExpression pattern) {
		backend().memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void closePattern(PatternDeclaration pattern) {

	}

	default void closePattern(PatternVariable pattern) {

	}
}
