package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlternative;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeclaration;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternLiteral;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.SumType;
import se.lth.cs.tycho.type.Type;

import java.util.Objects;
import java.util.stream.Collectors;

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

	default void openPattern(PatternLiteral pattern, String target, String deref, String member) {
		Type type = backend().types().type(pattern.getLiteral());
		emitter().emit("if (%s) {", code().compare(type, String.format("%s%s%s", target, deref, member), type, code().evaluate(pattern.getLiteral())));
		emitter().increaseIndentation();
		backend().memoryStack().enterScope();
	}

	default void openPattern(PatternAlias pattern, String target, String deref, String member) {
		Type type = backend().types().type(pattern.getExpression());
		String expr = code().evaluate(pattern.getExpression());
		String alias;
		if (pattern.getAlias() instanceof PatternVariable) {
			alias = backend().variables().name(((PatternVariable) pattern.getAlias()).getVariable());
			code().copy(type, alias, type, expr);
		} else if (pattern.getAlias() instanceof PatternBinding) {
			alias = backend().variables().declarationName(((PatternBinding) pattern.getAlias()).getDeclaration());
			if (type instanceof AlgebraicType) {
				backend().memoryStack().trackPointer(alias, type);
			}
			emitter().emit("%s = %s;", code().declaration(type, alias), backend().defaultValues().defaultValue(type));
			code().copy(type, alias, type, expr);
		} else {
			alias = expr;
		}
		emitter().emit("if (%s) {", code().compare(type, String.format("%s%s%s", target, deref, member), type, alias));
		emitter().increaseIndentation();
		backend().memoryStack().enterScope();
	}

	default void openPattern(PatternAlternative pattern, String target, String deref, String member) {
		emitter().emit("if (%s) {", pattern.getPatterns().stream().map(p -> {
			Expression expr;
			if (p instanceof PatternLiteral) {
				expr = ((PatternLiteral) p).getLiteral();
			} else {
				expr = ((PatternExpression) p).getExpression();
			}
			Type type = backend().types().type(expr);
			return code().compare(type, String.format("%s%s%s", target, deref, member), type, code().evaluate(expr));
		}).collect(Collectors.joining(" || ")));
		emitter().increaseIndentation();
		backend().memoryStack().enterScope();
	}

	default void openPattern(PatternList pattern, String target, String deref, String member) {
		for (int i = 0; i < pattern.getPatterns().size(); ++i) {
			openPattern(pattern.getPatterns().get(i), target, deref, String.format("%s.data[%d]", member, i));
		}
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

	default void closePattern(PatternLiteral pattern) {
		backend().memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void closePattern(PatternAlias pattern) {
		backend().memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void closePattern(PatternAlternative pattern) {
		backend().memoryStack().exitScope();
		emitter().decreaseIndentation();
		emitter().emit("}");
	}

	default void closePattern(PatternList pattern) {
		pattern.getPatterns().forEach(this::closePattern);
	}
}
