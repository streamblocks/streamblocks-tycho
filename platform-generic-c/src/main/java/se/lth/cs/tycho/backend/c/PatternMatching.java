package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
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
import se.lth.cs.tycho.ir.expr.pattern.PatternTuple;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.SumType;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.multij.BindingKind.LAZY;

@Module
public interface PatternMatching {

	@Binding(BindingKind.INJECTED)
	Backend backend();

	@Binding(LAZY)
	default Cases cases() {
		return MultiJ.from(Cases.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("emitter").to(backend().emitter())
				.bind("defaults").to(backend().defaultValues())
				.bind("trackable").to(backend().trackable())
				.bind("alternatives").to(alternatives())
				.instance();
	}

	@Binding(LAZY)
	default Alternatives alternatives() {
		return MultiJ.from(Alternatives.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("emitter").to(backend().emitter())
				.bind("patterns").to(patterns())
				.bind("trackable").to(backend().trackable())
				.instance();
	}

	@Binding(LAZY)
	default Patterns patterns() {
		return MultiJ.from(Patterns.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("emitter").to(backend().emitter())
				.bind("defaults").to(backend().defaultValues())
				.bind("trackable").to(backend().trackable())
				.instance();
	}

	default String evaluate(ExprCase expr) {
		return cases().evaluate(expr);
	}

	default void execute(StmtCase stmt) {
		cases().execute(stmt);
	}

	@Module
	interface Cases {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		DefaultValues defaults();
		@Binding(BindingKind.INJECTED)
		Trackable trackable();
		@Binding(BindingKind.INJECTED)
		Alternatives alternatives();

		default String evaluate(ExprCase expr) {
			trackable().enter();

			String scrutinee = code().variables().generateTemp();
			emitter().emit("%s = %s;", code().declaration(code().types().type(expr.getScrutinee()), scrutinee), code().evaluate(expr.getScrutinee()));

			String matched = code().variables().generateTemp();
			emitter().emit("%s = false;", code().declaration(BoolType.INSTANCE, matched));

			Type type = code().types().type(expr);
			String result = code().variables().generateTemp();

			emitter().emit("%s = %s;", code().declaration(type, result), defaults().defaultValue(type));

			trackable().track(result, type);

			Context ctx = new Context(result, matched, new Context.Scrutinee().withTarget(scrutinee));

			expr.getAlternatives().forEach(alternative -> {
				emitter().emit("if (!%s) {", ctx.getMatched());
				emitter().increaseIndentation();
				trackable().enter();

				alternatives().evaluate(alternative, ctx);

				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			});

			trackable().exit();
			return result;
		}

		default void execute(StmtCase stmt) {
			String scrutinee = code().variables().generateTemp();
			emitter().emit("%s = %s;", code().declaration(code().types().type(stmt.getScrutinee()), scrutinee), code().evaluate(stmt.getScrutinee()));

			String matched = code().variables().generateTemp();
			emitter().emit("%s = false;", code().declaration(BoolType.INSTANCE, matched));

			Context ctx = new Context(matched, new Context.Scrutinee().withTarget(scrutinee));

			stmt.getAlternatives().forEach(alternative -> {
				emitter().emit("if (!%s) {", ctx.getMatched());
				emitter().increaseIndentation();
				trackable().enter();

				alternatives().execute(alternative, ctx);

				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			});
		}
	}

	@Module
	interface Alternatives {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Trackable trackable();
		@Binding(BindingKind.INJECTED)
		Patterns patterns();

		default void evaluate(ExprCase.Alternative alternative, Context ctx) {
			patterns().start(alternative.getPattern(), ctx);

			alternative.getGuards().forEach(guard -> {
				emitter().emit("if (%s) {", code().evaluate(guard));
				emitter().increaseIndentation();
				trackable().enter();
			});

			code().copy(code().types().type(alternative.getExpression()), ctx.getResult(), code().types().type(alternative.getExpression()), code().evaluate(alternative.getExpression()));
			emitter().emit("%s = true;", ctx.getMatched());

			alternative.getGuards().forEach(guard -> {
				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			});

			patterns().end(alternative.getPattern());
		}

		default void execute(StmtCase.Alternative alternative, Context ctx) {
			patterns().start(alternative.getPattern(), ctx);

			alternative.getGuards().forEach(guard -> {
				emitter().emit("if (%s) {", code().evaluate(guard));
				emitter().increaseIndentation();
				trackable().enter();
			});

			alternative.getStatements().forEach(statement -> {
				code().execute(statement);
			});

			emitter().emit("%s = true;", ctx.getMatched());

			alternative.getGuards().forEach(guard -> {
				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			});

			patterns().end(alternative.getPattern());
		}
	}

	@Module
	interface Patterns {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Trackable trackable();
		@Binding(BindingKind.INJECTED)
		DefaultValues defaults();

		void start(Pattern pattern, Context ctx);

		default void start(PatternDeconstruction pattern, Context ctx) {
			Type type = code().types().type(pattern);
			if (type instanceof SumType) {
				SumType sum = (SumType) type;
				SumType.VariantType variant = sum.getVariants().stream().filter(v -> Objects.equals(v.getName(), pattern.getDeconstructor())).findAny().get();

				emitter().emit("if (%s->tag == tag_%s_%s) {", ctx.getScrutinee(), backend().algebraic().utils().mangle(sum.getName()), backend().algebraic().utils().mangle(variant.getName()));
				emitter().increaseIndentation();
				trackable().enter();

				for (int i = 0; i < variant.getFields().size(); ++i) {
					start(pattern.getPatterns().get(i), ctx.withScrutinee(ctx.getScrutinee()
							.withTarget(ctx.getScrutinee() + "->data." + backend().algebraic().utils().mangle(pattern.getDeconstructor()))
							.withAccess(".")
							.withMember(variant.getFields().get(i).getName())));
				}
			} else if (type instanceof ProductType) {
				ProductType product = (ProductType) type;
				for (int i = 0; i < product.getFields().size(); ++i) {
					start(pattern.getPatterns().get(i), ctx.withScrutinee(ctx.getScrutinee()
							.withTarget(ctx.getScrutinee().toString())
							.withAccess("->")
							.withMember(product.getFields().get(i).getName())));
				}
			}
		}

		default void start(PatternExpression pattern, Context ctx) {
			Type type = backend().types().type(pattern.getExpression());
			emitter().emit("if (%s) {", code().compare(type, ctx.getScrutinee().toString(), type, code().evaluate(pattern.getExpression())));
			emitter().increaseIndentation();
			trackable().enter();
		}

		default void start(PatternBinding pattern, Context ctx) {
			emitter().emit("%s = %s;", code().declaration(code().types().type(pattern), backend().variables().declarationName(pattern.getDeclaration())), ctx.getScrutinee().toString());
		}

		default void start(PatternVariable pattern, Context ctx) {
			emitter().emit("%s = %s;", backend().variables().name(pattern.getVariable()), ctx.getScrutinee().toString());
		}

		default void start(PatternWildcard pattern, Context ctx) {

		}

		default void start(PatternLiteral pattern, Context ctx) {
			Type type = backend().types().type(pattern.getLiteral());
			emitter().emit("if (%s) {", code().compare(type, ctx.getScrutinee().toString(), type, code().evaluate(pattern.getLiteral())));
			emitter().increaseIndentation();
			trackable().enter();
		}

		default void start(PatternAlias pattern, Context ctx) {
			Type type = backend().types().type(pattern.getExpression());
			String expr = code().evaluate(pattern.getExpression());

			String alias;
			if (pattern.getAlias() instanceof PatternVariable) {
				alias = backend().variables().name(((PatternVariable) pattern.getAlias()).getVariable());
				code().copy(type, alias, type, expr);
			} else if (pattern.getAlias() instanceof PatternBinding) {
				alias = backend().variables().declarationName(((PatternBinding) pattern.getAlias()).getDeclaration());
				trackable().track(alias, type);
				emitter().emit("%s = %s;", code().declaration(type, alias), defaults().defaultValue(type));
				code().copy(type, alias, type, expr);
			} else {
				alias = expr;
			}

			emitter().emit("if (%s) {", code().compare(type, ctx.getScrutinee().toString(), type, alias));
			emitter().increaseIndentation();
			trackable().enter();
		}

		default void start(PatternAlternative pattern, Context ctx) {
			emitter().emit("if (%s) {", pattern.getPatterns().stream().map(p -> {
				Expression expr;
				if (p instanceof PatternLiteral) {
					expr = ((PatternLiteral) p).getLiteral();
				} else {
					expr = ((PatternExpression) p).getExpression();
				}
				Type type = backend().types().type(expr);
				return code().compare(type, ctx.getScrutinee().toString(), type, code().evaluate(expr));
			}).collect(Collectors.joining(" || ")));

			emitter().increaseIndentation();
			trackable().enter();
		}

		default void start(PatternList pattern, Context ctx) {
			for (int i = 0; i < pattern.getPatterns().size(); ++i) {
				start(pattern.getPatterns().get(i), ctx.withScrutinee(ctx.getScrutinee().withMember(String.format("%s.data[%d]", ctx.getScrutinee().getMember(), i))));
			}
		}

		default void start(PatternTuple pattern, Context ctx) {
			ProductType product = backend().tuples().convert().apply((TupleType) backend().types().type(pattern));
			for (int i = 0; i < product.getFields().size(); ++i) {
				start(pattern.getPatterns().get(i), ctx.withScrutinee(ctx.getScrutinee()
						.withTarget(ctx.getScrutinee().toString())
						.withAccess("->")
						.withMember(product.getFields().get(i).getName())));
			}
		}

		void end(Pattern pattern);

		default void end(PatternDeconstruction pattern) {
			Type type = code().types().type(pattern);
			if (type instanceof SumType) {
				trackable().exit();
				emitter().decreaseIndentation();
				emitter().emit("}");
			}
			pattern.getPatterns().forEach(this::end);
		}

		default void end(PatternExpression pattern) {
			trackable().exit();
			emitter().decreaseIndentation();
			emitter().emit("}");
		}

		default void end(PatternDeclaration pattern) {

		}

		default void end(PatternVariable pattern) {

		}

		default void end(PatternLiteral pattern) {
			trackable().exit();
			emitter().decreaseIndentation();
			emitter().emit("}");
		}

		default void end(PatternAlias pattern) {
			trackable().exit();
			emitter().decreaseIndentation();
			emitter().emit("}");
		}

		default void end(PatternAlternative pattern) {
			trackable().exit();
			emitter().decreaseIndentation();
			emitter().emit("}");
		}

		default void end(PatternTuple pattern) {
			pattern.getPatterns().forEach(this::end);
		}

		default void end(PatternList pattern) {
			pattern.getPatterns().forEach(this::end);
		}
	}

	class Context {

		static class Scrutinee {

			private final String target;
			private final String access;
			private final String member;

			public Scrutinee() {
				this("", "", "");
			}

			public Scrutinee(String target, String access, String member) {
				this.target = target;
				this.access = access;
				this.member = member;
			}

			public Scrutinee copy(String target, String access, String member) {
				return new Scrutinee(target, access, member);
			}

			public String getTarget() {
				return target;
			}

			public Scrutinee withTarget(String target) {
				return copy(target, getAccess(), getMember());
			}

			public String getAccess() {
				return access;
			}

			public Scrutinee withAccess(String access) {
				return copy(getTarget(), access, getMember());
			}

			public String getMember() {
				return member;
			}

			public Scrutinee withMember(String member) {
				return copy(getTarget(), getAccess(), member);
			}

			@Override
			public String toString() {
				return member.isEmpty() ? target : target + access + member;
			}
		}

		private final String result;
		private final String matched;
		private final Scrutinee scrutinee;

		public Context(String matched, Scrutinee matching) {
			this("", matched, matching);
		}

		public Context(String result, String matched, Scrutinee scrutinee) {
			this.result = result;
			this.matched = matched;
			this.scrutinee = scrutinee;
		}

		public Context copy(String result, String matched, Scrutinee scrutinee) {
			return new Context(result, matched, scrutinee);
		}

		public String getResult() {
			return result;
		}

		public String getMatched() {
			return matched;
		}

		public Scrutinee getScrutinee() {
			return scrutinee;
		}

		public Context withScrutinee(Scrutinee scrutinee) {
			return copy(getResult(), getMatched(), scrutinee);
		}
	}
}
