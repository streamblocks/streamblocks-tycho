package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.pattern.Alternative;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstructor;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
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
		String expr = code().variables().generateTemp();
		emitter().emit("%s = %s;", code().declaration(code().types().type(caseExpr.getExpression()), expr), code().evaluate(caseExpr.getExpression()));
		String match = code().variables().generateTemp();
		emitter().emit("%s = false;", code().declaration(BoolType.INSTANCE, match));
		String result = code().variables().generateTemp();
		emitter().emit("%s;", code().declaration(code().types().type(caseExpr), result));
		caseExpr.getAlternatives().forEach(alternative -> {
			emitter().emit("if (!%s) {", match);
			emitter().increaseIndentation();
			alternative(alternative, expr, result, match);
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		emitter().emit("if (!%s) {", match);
		emitter().increaseIndentation();
		code().copy(code().types().type(caseExpr), result, code().types().type(caseExpr.getDefault()), code().evaluate(caseExpr.getDefault()));
		emitter().decreaseIndentation();
		emitter().emit("}");
		return result;
	}

	default void alternative(Alternative alternative, String expr, String result, String match) {
		openPattern(alternative.getPattern(), expr, "");
		alternative.getGuards().forEach(guard -> {
			emitter().emit("if (%s) {", code().evaluate(guard));
			emitter().increaseIndentation();
		});
		emitter().emit("%s = %s;", result, code().evaluate(alternative.getExpression()));
		emitter().emit("%s = true;", match);
		alternative.getGuards().forEach(guard -> {
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		closePattern(alternative.getPattern());
	}

	void openPattern(Pattern pattern, String target, String member);

	default void openPattern(PatternDeconstructor pattern, String target, String member) {
		Type type = code().types().type(pattern);
		if (type instanceof SumType) {
			SumType sum = (SumType) type;
			SumType.VariantType variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), pattern.getName())).findAny().get();
			emitter().emit("if (%s.tag == tag_%s_%s) {", member == "" ? target : target + "." + member, sum.getName(), variant.getName());
			emitter().increaseIndentation();
			for (int i = 0; i < variant.getFields().size(); ++i) {
				openPattern(pattern.getPatterns().get(i), (member == "" ? target : target + "." + member) + ".value." + pattern.getName(), variant.getFields().get(i).getName());
			}
		} else if (type instanceof ProductType) {
			ProductType product = (ProductType) type;
			for (int i = 0; i < product.getFields().size(); ++i) {
				openPattern(pattern.getPatterns().get(i), member == "" ? target : target + "." + member, product.getFields().get(i).getName());
			}
		}
	}

	default void openPattern(PatternExpression pattern, String target, String member) {
		emitter().emit("if (%s.%s == (%s)) {", target, member, code().evaluate(pattern.getExpression()));
		emitter().increaseIndentation();
	}

	default void openPattern(PatternVariable pattern, String target, String member) {
		emitter().emit("%s = %s.%s;", code().declaration(code().types().type(pattern), pattern.getDeclaration().getName()), target, member);
	}

	default void openPattern(PatternWildcard pattern, String target, String member) {

	}

	void closePattern(Pattern pattern);

	default void closePattern(PatternDeconstructor pattern) {
		Type type = code().types().type(pattern);
		if (type instanceof SumType) {
			emitter().decreaseIndentation();
			emitter().emit("}");
		}
		pattern.getPatterns().forEach(this::closePattern);
	}

	default void closePattern(PatternExpression pattern) {
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void closePattern(PatternVariable pattern) {

	}

	default void closePattern(PatternWildcard pattern) {

	}
}
