package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.decoration.StructuralEquality;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlternative;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeclaration;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternLiteral;
import se.lth.cs.tycho.ir.expr.pattern.PatternTuple;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.AliasType;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.FieldType;
import se.lth.cs.tycho.type.IntType;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.RealType;
import se.lth.cs.tycho.type.SumType;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CaseAnalysisPhase implements Phase {

	@Override
	public String getDescription() {
		return "Analyses exhaustivity and unreachability of cases";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {

		Reporter reporter = context.getReporter();

		Types types = task.getModule(Types.key);

		TreeShadow tree = task.getModule(TreeShadow.key);

		Flatten flatten = MultiJ.from(Flatten.class).instance();

		SubType subtype = MultiJ.from(SubType.class).instance();

		Decompose decompose = MultiJ.from(Decompose.class).instance();

		Signature signature = MultiJ.from(Signature.class).instance();

		EqualType equal = MultiJ.from(EqualType.class).instance();

		Unit unit = MultiJ.from(Unit.class).instance();

		Recursive recursive = MultiJ.from(Recursive.class).instance();

		Printer printer = MultiJ.from(Printer.class)
				.bind("flatten").to(flatten)
				.instance();

		Project project = MultiJ.from(Project.class)
				.bind("types").to(types)
				.bind("unit").to(unit)
				.bind("recursive").to(recursive)
				.instance();

		Satisfiable satisfiable = MultiJ.from(Satisfiable.class)
				.bind("signature").to(signature)
				.bind("subtype").to(subtype)
				.instance();

		SpaceOps ops = MultiJ.from(SpaceOps.class)
				.bind("subtype").to(subtype)
				.bind("equal").to(equal)
				.bind("decompose").to(decompose)
				.bind("signature").to(signature)
				.instance();

		Exhaustivity exhaustivity = MultiJ.from(Exhaustivity.class)
				.bind("types").to(types)
				.bind("project").to(project)
				.bind("flatten").to(flatten)
				.bind("ops").to(ops)
				.bind("satisfiable").to(satisfiable)
				.bind("printer").to(printer)
				.bind("reporter").to(reporter)
				.bind("tree").to(tree)
				.instance();

		Redundancy redundancy = MultiJ.from(Redundancy.class)
				.bind("types").to(types)
				.bind("project").to(project)
				.bind("ops").to(ops)
				.bind("reporter").to(reporter)
				.bind("tree").to(tree)
				.instance();

		SpaceLogic logic = MultiJ.from(SpaceLogic.class)
				.bind("exhaustivity").to(exhaustivity)
				.bind("redundancy").to(redundancy)
				.instance();

		Analysis analysis = MultiJ.from(Analysis.class)
				.bind("logic").to(logic)
				.bind("tree").to(tree)
				.instance();

		analysis.analyse(task);

		return task;
	}

	@Module
	interface Analysis {

		@Binding(BindingKind.INJECTED)
		SpaceLogic logic();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		default void analyse(IRNode node) {
			check(node);
			node.forEachChild(this::analyse);
		}

		default void check(IRNode node) {

		}

		default void check(ExprCase expr) {
			if (fromMatch(expr)) {
				return;
			}
			logic().checkExhaustivity(expr);
			logic().checkRedundancy(expr);
		}

		default boolean fromMatch(ExprCase expr) {
			return tree().parent(expr) instanceof Match;
		}
	}

	static abstract class Space {

		static final Empty EMPTY = new Empty();

		static class Universe extends Space {

			private final Type type;

			private Universe(Type type) {
				this.type = type;
			}

			public static Universe of(Type type) {
				return new Universe(type);
			}

			public Type type() {
				return type;
			}
		}

		static class Singleton extends Space {

			private final Type type;
			private final Expression expression;

			private Singleton(Type type, Expression expression) {
				this.type = type;
				this.expression = expression;
			}

			public static Singleton of(Type type, Expression expression) {
				return new Singleton(type, expression);
			}

			public Type type() {
				return type;
			}

			public Expression expression() {
				return expression;
			}
		}

		static class Product extends Space {

			private final Type type;
			private final Type apply;
			private final List<Space> spaces;

			private Product(Type type, Type apply, List<Space> spaces) {
				this.type = type;
				this.apply = apply;
				this.spaces = spaces;
			}

			public static Product of(Type type, Type term, List<Space> spaces) {
				return new Product(type, term, spaces);
			}

			public Type type() {
				return type;
			}

			public Type apply() {
				return apply;
			}

			public List<Space> spaces() {
				return spaces;
			}
		}

		static class Union extends Space {

			private final List<Space> spaces;

			private Union(List<Space> spaces) {
				this.spaces = spaces;
			}

			public static Union of(List<Space> spaces) {
				return new Union(spaces);
			}

			public List<Space> spaces() {
				return spaces;
			}
		}

		static class Empty extends Space {

			private Empty() {

			}
		}
	}

	@Module
	interface SpaceLogic {

		@Binding(BindingKind.INJECTED)
		Exhaustivity exhaustivity();

		@Binding(BindingKind.INJECTED)
		Redundancy redundancy();

		default void checkExhaustivity(ExprCase expr) {
			exhaustivity().check(expr);
		}

		default void checkRedundancy(ExprCase expr) {
			redundancy().check(expr);
		}
	}

	@Module
	interface Exhaustivity {

		@Binding(BindingKind.INJECTED)
		Types types();

		@Binding(BindingKind.INJECTED)
		Project project();

		@Binding(BindingKind.INJECTED)
		Flatten flatten();

		@Binding(BindingKind.INJECTED)
		SpaceOps ops();

		@Binding(BindingKind.INJECTED)
		Satisfiable satisfiable();

		@Binding(BindingKind.INJECTED)
		Printer printer();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		default void check(ExprCase expr) {
			Space targetSpace = Space.Universe.of(types().type(expr.getExpression()));

			Space patternSpace = expr.getAlternatives().stream()
					.map(a -> a.getGuards().isEmpty() ? project().apply(a.getPattern()) : Space.EMPTY)
					.reduce((a, b) -> Space.Union.of(Arrays.asList(a, b))).get();

			List<Space> uncovered = flatten()
					.apply(ops().simplify(ops().minus(targetSpace, patternSpace), true))
					.stream()
					.filter(s -> s != Space.EMPTY && satisfiable().test(s))
					.collect(Collectors.toList());

			if (!uncovered.isEmpty()) {
				String message = new StringBuilder()
						.append("case may not be exhaustive.")
						.append(System.lineSeparator())
						.append(System.lineSeparator())
						.append(String.format("It would fail on pattern%s: %s", uncovered.size() != 1 ? "s" : "" , printer().apply(Space.Union.of(uncovered))))
						.toString();
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, message, sourceUnit(expr), expr.getExpression()));
			}
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}

	@Module
	interface Redundancy {

		@Binding(BindingKind.INJECTED)
		Types types();

		@Binding(BindingKind.INJECTED)
		Project project();

		@Binding(BindingKind.INJECTED)
		SpaceOps ops();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		default void check(ExprCase expr) {
			Space targetSpace = Space.Universe.of(types().type(expr.getExpression()));

			for (int i = 1; i < expr.getAlternatives().size(); ++i) {
				Space previousSpace = expr.getAlternatives().stream()
						.limit(i)
						.map(a -> a.getGuards().isEmpty() ? project().apply(a.getPattern()) : Space.EMPTY)
						.reduce((a, b) -> Space.Union.of(Arrays.asList(a, b))).get();

				Space currentSpace = project()
						.apply(expr.getAlternatives().get(i).getPattern());

				Space covered = ops().
						simplify(ops().intersect(currentSpace, targetSpace), false);

				if (ops().isSubSpace(covered, previousSpace)) {
					reporter().report(new Diagnostic(Diagnostic.Kind.WARNING, "Unreachable alternative.", sourceUnit(expr), expr.getAlternatives().get(i).getPattern()));
				}
			}
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}

	@Module
	interface Project {

		@Binding(BindingKind.INJECTED)
		Types types();

		@Binding(BindingKind.INJECTED)
		Unit unit();

		@Binding(BindingKind.INJECTED)
		Recursive recursive();

		Space apply(Pattern pattern);

		default Space apply(PatternLiteral pattern) {
			if (types().type(pattern) instanceof BoolType) {
				return Space.Universe.of(new ConstantType(Boolean.valueOf(pattern.getLiteral().getText())));
			} else {
				return Space.Singleton.of(types().type(pattern), pattern.getLiteral());
			}
		}

		default Space apply(PatternDeclaration pattern) {
			return Space.Universe.of(types().type(pattern));
		}

		default Space apply(PatternVariable pattern) {
			return Space.Universe.of(types().type(pattern));
		}

		default Space apply(PatternAlias pattern) {
			return apply(new PatternExpression(pattern.getExpression()));
		}

		default Space apply(PatternAlternative pattern) {
			return Space.Union.of(pattern.getPatterns().map(this::apply));
		}

		default Space apply(PatternExpression pattern) {
			if (pattern.getExpression() instanceof ExprLiteral) {
				return apply(new PatternLiteral((ExprLiteral) pattern.getExpression()));
			} else if (!(recursive().test(types().type(pattern))) && unit().test(types().type(pattern))) {
				return Space.Universe.of(types().type(pattern));
			} else {
				return Space.Singleton.of(types().type(pattern), pattern.getExpression());
			}
		}

		default Space apply(PatternList pattern) {
			ListType type = (ListType) types().type(pattern);
			int provided = pattern.getPatterns().size();
			int expected = type.getSize().getAsInt();
			Stream<Space> remaining = Stream.empty();
			if (provided < expected) {
				remaining = Collections.nCopies(expected - provided, Space.Universe.of(type.getElementType())).stream().map(Space.class::cast);
			}
			return Space.Product.of(types().type(pattern), types().type(pattern), Stream.concat(pattern.getPatterns().stream().map(this::apply), remaining).collect(Collectors.toList()));
		}

		default Space apply(PatternTuple pattern) {
			Type type = types().type(pattern);
			return Space.Product.of(type, type, pattern.getPatterns().map(this::apply));
		}

		default Space apply(PatternDeconstruction pattern) {
			Type type = types().type(pattern);
			Type apply;
			if (type instanceof ProductType) {
				apply = type;
			} else {
				apply = ((SumType) type).getVariants().stream().filter(v -> Objects.equals(v.getName(), pattern.getName())).findAny().get();
			}
			return Space.Product.of(type, apply, pattern.getPatterns().map(this::apply));
		}
	}

	@Module
	interface Flatten {

		default List<Space> apply(Space space) {
			return Collections.singletonList(space);
		}

		default List<Space> apply(Space.Product space) {
			List<List<Space>> flattened = space.spaces().stream().map(this::apply).collect(Collectors.toList());
			if (flattened.isEmpty()) { return Collections.singletonList(Space.Product.of(space.type(), space.apply(), Collections.emptyList())); }
			else { return flattened.stream()
					.reduce(new ArrayList<>(), (acc, flat) -> {
						if (acc.isEmpty()) { return flat.stream().map(s -> Space.Product.of(space.type(), space.apply(), Collections.singletonList(s))).collect(Collectors.toList()); }
						else {
							List<Space> newAcc = new ArrayList<>();
							for (Space a : acc) {
								Space.Product p = (Space.Product) a;
								for (Space f : flat) {
									List<Space> newSpaces = new ArrayList<>(p.spaces());
									newSpaces.add(f);
									newAcc.add(Space.Product.of(p.type(), p.apply(), newSpaces));
								}
							}
							return newAcc;
						}
					});
			}
		}

		default List<Space> apply(Space.Union space) {
			return space.spaces().stream().flatMap(s -> apply(s).stream()).collect(Collectors.toList());
		}
	}

	@Module
	interface SpaceOps {

		@Binding(BindingKind.INJECTED)
		SubType subtype();

		@Binding(BindingKind.INJECTED)
		EqualType equal();

		@Binding(BindingKind.INJECTED)
		Decompose decompose();

		@Binding(BindingKind.INJECTED)
		Signature signature();

		/* INTERSECT */

		default Space intersect(Space a, Space b) {
			if (a instanceof Space.Empty) {
				return Space.EMPTY;
			} else if (b instanceof Space.Empty) {
				return Space.EMPTY;
			} else if (b instanceof Space.Union) {
				return Space.Union.of(((Space.Union) b).spaces().stream().map(s -> intersect(a, s)).filter(s -> s != Space.EMPTY).collect(Collectors.toList()));
			} else if (a instanceof Space.Union) {
				return Space.Union.of(((Space.Union) a).spaces().stream().map(s -> intersect(s, b)).filter(s -> s != Space.EMPTY).collect(Collectors.toList()));
			} else if (a instanceof Space.Universe && b instanceof Space.Universe) {
				Type tp1 = ((Space.Universe) a).type();
				Type tp2 = ((Space.Universe) b).type();
				if (subtype().test(tp1, tp2)) return a;
				else if (subtype().test(tp2, tp1)) return b;
				else if (decompose().test(tp1)) return intersectTryDecompose1(tp1, b);
				else if (decompose().test(tp2)) return intersectTryDecompose2(tp2, a);
				else return Space.EMPTY;
			} else if (a instanceof Space.Universe && b instanceof Space.Product) {
				Type tp1 = ((Space.Universe) a).type();
				Type tp2 = ((Space.Product) b).apply();
				if (subtype().test(tp2, tp1)) return b;
				else if (subtype().test(tp1, tp2)) return b;
				else if (decompose().test(tp1)) return intersectTryDecompose1(tp1, b);
				else return Space.EMPTY;
			} else if (a instanceof Space.Product && b instanceof Space.Universe) {
				Type tp1 = ((Space.Product) a).apply();
				Type tp2 = ((Space.Universe) b).type();
				if (subtype().test(tp1, tp2)) return a;
				else if (subtype().test(tp2, tp1)) return a;
				else if (decompose().test(tp2)) return intersectTryDecompose2(tp2, a);
				else return Space.EMPTY;
			} else if (a instanceof Space.Product && b instanceof Space.Product) {
				if (!(equal().test(((Space.Product) a).apply(), ((Space.Product) b).apply()))) return Space.EMPTY;
				else if (IntStream.range(0, ((Space.Product) a).spaces().size()).anyMatch(i -> simplify(intersect(((Space.Product) a).spaces().get(i), ((Space.Product) b).spaces().get(i)), false) == Space.EMPTY)) return Space.EMPTY;
				else return Space.Product.of(((Space.Product) a).type(), ((Space.Product) a).apply(), IntStream.range(0, ((Space.Product) a).spaces().size()).mapToObj(i -> intersect(((Space.Product) a).spaces().get(i), ((Space.Product) b).spaces().get(i))).collect(Collectors.toList()));
			} else if (a instanceof Space.Singleton) {
				return a;
			} else if (b instanceof Space.Singleton) {
				return b;
			} else {
				throw new RuntimeException();
			}
		}

		default Space intersectTryDecompose1(Type type, Space s) {
			return intersect(Space.Union.of(decompose().apply(type)), s);
		}

		default Space intersectTryDecompose2(Type type, Space s) {
			return intersect(s, Space.Union.of(decompose().apply(type)));
		}

		/* MINUS */

		default Space minus(Space a, Space b) {
			if (a instanceof Space.Empty) {
				return Space.EMPTY;
			} else if (b instanceof Space.Empty) {
				return Space.EMPTY;
			} else if (a instanceof Space.Universe && b instanceof Space.Universe) {
				Type tp1 = ((Space.Universe) a).type();
				Type tp2 = ((Space.Universe) b).type();
				if (subtype().test(tp1, tp2)) return Space.EMPTY;
				else if (decompose().test(tp1)) return minusTryDecompose1(tp1, b);
				else if (decompose().test(tp2)) return minusTryDecompose2(tp2, a);
				else return a;
			} else if (a instanceof Space.Universe && b instanceof Space.Product) {
				Type tp1 = ((Space.Universe) a).type();
				Type tp2 = ((Space.Product) b).apply();
				if (subtype().test(tp1, tp2)) return minus(Space.Product.of(((Space.Product) b).type(), ((Space.Product) b).apply(), signature().apply(((Space.Product) b).apply()).stream().map(type -> Space.Universe.of(type)).collect(Collectors.toList())), b);
				else if (decompose().test(tp1)) return minusTryDecompose1(tp1, b);
				else return a;
			} else if (b instanceof Space.Union) {
				return ((Space.Union) b).spaces().stream().reduce(a, this::minus);
			} else if (a instanceof Space.Union) {
				return Space.Union.of(((Space.Union) a).spaces().stream().map(s -> minus(s, b)).collect(Collectors.toList()));
			} else if (a instanceof Space.Product && b instanceof Space.Universe) {
				Type tp1 = ((Space.Product) a).apply();
				Type tp2 = ((Space.Universe) b).type();
				if (subtype().test(tp1, tp2)) return Space.EMPTY;
				else if (simplify(a, false) == Space.EMPTY) return Space.EMPTY;
				else if (decompose().test(tp2)) return minusTryDecompose2(tp2, a);
				else return a;
			} else if (a instanceof Space.Product && b instanceof Space.Product) {
				if (!equal().test(((Space.Product) a).apply(), ((Space.Product) b).apply())) return a;
				else if (IntStream.range(0, ((Space.Product) a).spaces().size()).anyMatch(i -> simplify(intersect(((Space.Product) a).spaces().get(i), ((Space.Product) b).spaces().get(i)), false) == Space.EMPTY))
					return a;
				else if (IntStream.range(0, ((Space.Product) a).spaces().size()).allMatch(i -> isSubSpace(((Space.Product) a).spaces().get(i), ((Space.Product) b).spaces().get(i))))
					return Space.EMPTY;
				else {
					List<Space> difference = IntStream.range(0, ((Space.Product) a).spaces().size()).mapToObj(i -> minus(((Space.Product) a).spaces().get(i), ((Space.Product) b).spaces().get(i))).collect(Collectors.toList());
					return Space.Union.of(
							IntStream.range(0, difference.size()).mapToObj(i -> {
								List<Space> updated = new ArrayList<>(((Space.Product) a).spaces());
								updated.set(i, difference.get(i));
								return Space.Product.of(((Space.Product) a).type(), ((Space.Product) a).apply(), updated);
							}).collect(Collectors.toList())
					);
				}
			} else if (a instanceof Space.Singleton && b instanceof Space.Singleton) {
				return StructuralEquality.equals(((Space.Singleton) a).expression(), ((Space.Singleton) b).expression()) ? Space.EMPTY : a;
			} else if (a instanceof Space.Singleton && b instanceof Space.Universe) {
				return subtype().test(((Space.Singleton) a).type(), ((Space.Universe) b).type()) ? Space.EMPTY : a;
			} else if (a instanceof Space.Singleton) {
				return a;
			} else if (b instanceof Space.Singleton) {
				return a;
			} else {
				throw new RuntimeException();
			}
		}

		default Space minusTryDecompose1(Type type, Space s) {
			return minus(Space.Union.of(decompose().apply(type)), s);
		}

		default Space minusTryDecompose2(Type type, Space s) {
			return minus(s, Space.Union.of(decompose().apply(type)));
		}

		/* SIMPLIFY */

		default Space simplify(Space space, boolean aggressive) {
			return space;
		}

		default Space simplify(Space.Product space, boolean aggressive) {
			Space.Product sp = Space.Product.of(space.type(), space.apply(), space.spaces().stream().map(s -> simplify(s, false)).collect(Collectors.toList()));
			if (sp.spaces().contains(Space.EMPTY)) return Space.EMPTY;
			else if (decompose().test(sp.type()) && decompose().apply(sp.type()).isEmpty()) return Space.EMPTY;
			else return sp;
		}

		default Space simplify(Space.Union space, boolean aggressive) {
			List<Space> set = space.spaces().stream().map(s -> simplify(s, false)).flatMap(s -> {
				if (s instanceof Space.Union) return ((Space.Union) s).spaces().stream();
				else return Stream.of(s);
			}).filter(s -> s != Space.EMPTY).collect(Collectors.toList());

			if (set.isEmpty()) return Space.EMPTY;
			else if (set.size() == 1) return set.get(0);
			else if (aggressive && space.spaces().size() < 5) {
				Optional<Tuple<Space, List<Space>>> res = set.stream()
						.map(sp -> Tuple.of(sp, set.stream().filter(s -> s != sp).collect(Collectors.toList())))
						.filter(t -> isSubSpace(t.first(), Space.Union.of(t.second())))
						.findAny();
				if (!res.isPresent()) return Space.Union.of(set);
				else return simplify(Space.Union.of(res.get().second()), aggressive);
			}
			else return Space.Union.of(set);
		}

		default Space simplify(Space.Universe space, boolean aggressive) {
			Type tp = space.type();
			if (decompose().test(tp) && decompose().apply(tp).isEmpty()) return Space.EMPTY;
			else return space;
		}

		/* SUBSPACE */

		default boolean isSubSpace(Space a, Space b) {
			Space _a = simplify(a, false);
			if (_a instanceof Space.Empty) {
				return true;
			} else if (b instanceof Space.Empty) {
				return false;
			} else if (_a instanceof Space.Union) {
				return ((Space.Union) _a).spaces().stream().allMatch(s -> isSubSpace(s, b));
			} else if (_a instanceof Space.Universe && b instanceof Space.Universe) {
				return subtype().test(((Space.Universe) _a).type(), ((Space.Universe) b).type());
			} else if (_a instanceof Space.Universe && b instanceof Space.Union) {
				return ((Space.Union) b).spaces().stream().anyMatch(s -> isSubSpace(a, s)) || isSubSpaceTryDecompose1(((Space.Universe) _a).type(), b);
			} else if (b instanceof Space.Union) {
				return simplify(minus(a, b), false) == Space.EMPTY;
			} else if (_a instanceof Space.Product && b instanceof Space.Universe) {
				return subtype().test(((Space.Product) _a).apply(), ((Space.Universe) b).type());
			} else if (_a instanceof Space.Universe && b instanceof Space.Product) {
				return subtype().test(((Space.Universe) _a).type(), ((Space.Product) b).apply()) && isSubSpace(Space.Product.of(((Space.Product) b).type(), ((Space.Product) b).apply(), signature().apply(((Space.Product) b).apply()).stream().map(type -> Space.Universe.of(type)).collect(Collectors.toList())), b);
			} else if (_a instanceof Space.Product && b instanceof Space.Product) {
				return equal().test(((Space.Product) _a).apply(), ((Space.Product) b).apply()) && IntStream.range(0, ((Space.Product) _a).spaces().size()).allMatch(i -> isSubSpace(((Space.Product) _a).spaces().get(i), ((Space.Product) b).spaces().get(i)));
			} else if (_a instanceof Space.Singleton && b instanceof Space.Singleton) {
				return StructuralEquality.equals(((Space.Singleton) _a).expression(), ((Space.Singleton) b).expression());
			} else if (_a instanceof Space.Singleton && b instanceof Space.Universe) {
				return subtype().test(((Space.Singleton) _a).type(), ((Space.Universe) b).type());
			} else {
				return false;
			}
		}

		default boolean isSubSpaceTryDecompose1(Type type, Space space) {
			return decompose().test(type) && isSubSpace(Space.Union.of(decompose().apply(type)), space);
		}
	}

	@Module
	interface SubType {

		default boolean test(Type a, Type b) {
			return a.equals(b);
		}

		default boolean test(IntType a, IntType b) {
			return true;
		}

		default boolean test(RealType a, RealType b) {
			return true;
		}

		default boolean test(ListType a, ListType b) {
			return true;
		}

		default boolean test(TupleType a, TupleType b) {
			return true;
		}

		default boolean test(SumType.VariantType a, SumType b) {
			return b.getVariants().contains(a);
		}

		default boolean test(ConstantType a, BoolType b) {
			return true;
		}

		default boolean test(AliasType a, AliasType b) {
			return test(a.getType(), b.getType());
		}

		default boolean test(AliasType a, Type b) {
			return test(a.getType(), b);
		}

		default boolean test(Type a, AliasType b) {
			return test(a, b.getType());
		}
	}

	@Module
	interface EqualType {

		default boolean test(Type a, Type b) {
			return false;
		}

		default boolean test(ListType a, ListType b) {
			return true;
		}

		default boolean test(TupleType a, TupleType b) {
			return true;
		}

		default boolean test(AlgebraicType a, AlgebraicType b) {
			return a.equals(b);
		}

		default boolean test(SumType.VariantType a, SumType.VariantType b) {
			return a.equals(b);
		}

		default boolean test(AliasType a, AliasType b) {
			return test(a.getType(), b.getType());
		}

		default boolean test(AliasType a, Type b) {
			return test(a.getType(), b);
		}

		default boolean test(Type a, AliasType b) {
			return test(a, b.getType());
		}
	}

	@Module
	interface Decompose {

		default boolean test(Type type) {
			return false;
		}

		default boolean test(BoolType type) {
			return true;
		}

		default boolean test(SumType type) {
			return true;
		}

		default boolean test(ProductType type) {
			return true;
		}

		default boolean test(AliasType type) {
			return test(type.getType());
		}

		List<Space> apply(Type type);

		default List<Space> apply(BoolType type) {
			return Arrays.asList(Space.Universe.of(new ConstantType(false)), Space.Universe.of(new ConstantType(true)));
		}

		default List<Space> apply(SumType type) {
			return type.getVariants().stream().map(variant -> Space.Universe.of(variant)).collect(Collectors.toList());
		}

		default List<Space> apply(ProductType type) {
			return Collections.singletonList(Space.Universe.of(type));
		}

		default List<Space> apply(AliasType type) {
			return apply(type.getType());
		}
	}

	@Module
	interface Signature {

		List<Type> apply(Type apply);

		default List<Type> apply(ListType type) {
			return Collections.nCopies(type.getSize().getAsInt(), type.getElementType());
		}

		default List<Type> apply(TupleType type) {
			return type.getTypes();
		}

		default List<Type> apply(ProductType type) {
			return type.getFields().stream().map(FieldType::getType).collect(Collectors.toList());
		}

		default List<Type> apply(SumType.VariantType type) {
			return type.getFields().stream().map(FieldType::getType).collect(Collectors.toList());
		}

		default List<Type> apply(AliasType type) {
			return apply(type.getType());
		}
	}

	@Module
	interface Unit {

		default boolean test(Type type) {
			return false;
		}

		default boolean test(ProductType type) {
			return type.getFields().stream().allMatch(field -> test(field.getType()));
		}

		default boolean test(SumType type) {
			return type.getVariants().size() == 1 && type.getVariants().get(0).getFields().stream().allMatch(field -> test(field.getType()));
		}

		default boolean test(AliasType type) {
			return test(type.getType());
		}

		default boolean test(ListType type) {
			return test(type.getElementType());
		}

		default boolean test(TupleType type) {
			return type.getTypes().stream().allMatch(tpe -> test(tpe));
		}
	}

	@Module
	interface Recursive {

		default boolean test(Type type) {
			return visit(type, new HashSet<>());
		}

		default boolean visit(Type type, Set<Type> visited) {
			return false;
		}

		default boolean visit(ProductType type, Set<Type> visited) {
			return visited.add(type) || type.getFields().stream().anyMatch(f -> visit(f.getType(), visited));
		}

		default boolean visit(SumType type, Set<Type> visited) {
			return visited.add(type) || type.getVariants().stream().flatMap(v -> v.getFields().stream()).anyMatch(f -> visit(f.getType(), visited));
		}

		default boolean visit(AliasType type, Set<Type> visited) {
			return visit(type.getType(), visited);
		}

		default boolean visit(ListType type, Set<Type> visited) {
			return visited.add(type) || visit(type.getElementType(), visited);
		}

		default boolean visit(TupleType type, Set<Type> visited) {
			return visited.add(type) || type.getTypes().stream().anyMatch(tpe -> visit(tpe, visited));
		}
	}

	@Module
	interface Satisfiable {

		@Binding(BindingKind.INJECTED)
		Signature signature();

		@Binding(BindingKind.INJECTED)
		SubType subtype();

		AssertionError impossible = new AssertionError("`satisfiable` only accepts flattened space.");

		default boolean test(Space space) {
			return check(constraints(space));
		}

		default List<Tuple<Type, Type>> constraints(Space space) {
			throw impossible;
		}

		default List<Tuple<Type, Type>> constraints(Space.Universe space) {
			return Collections.emptyList();
		}

		default List<Tuple<Type, Type>> constraints(Space.Singleton space) {
			return Collections.emptyList();
		}

		default List<Tuple<Type, Type>> constraints(Space.Product space) {
			List<Type> types = signature().apply(space.apply());
			return IntStream.range(0, types.size()).mapToObj(i -> i).flatMap(i -> {
				Space s = space.spaces().get(i);
				Type tp = types.get(i);
				if (s instanceof Space.Product) {
					List<Tuple<Type, Type>> constrs = constraints(s);
					constrs.add(Tuple.of(((Space.Product) s).type(), tp));
					return constrs.stream();
				} else if (s instanceof Space.Universe) {
					return Stream.of(Tuple.of(((Space.Universe) s).type(), tp));
				} else {
					throw impossible;
				}
			}).collect(Collectors.toList());
		}

		default boolean check(List<Tuple<Type, Type>> constraints) {
			return constraints.stream().allMatch(c -> subtype().test(c.first(), c.second()));
		}
	}

	@Module
	interface Printer {

		@Binding(BindingKind.INJECTED)
		Flatten flatten();

		default String apply(Space space) {
			return flatten().apply(space).stream().map(s -> doApply(s, false)).distinct().collect(Collectors.joining(", "));
		}

		String doApply(Space space, boolean flattenList);

		default String doApply(Space.Empty space, boolean flattenList) {
			return "";
		}

		default String doApply(Space.Universe space, boolean flattenList) {
			Type type = space.type();
			if (type instanceof ConstantType) {
				return "" + ((ConstantType) type).value();
			} else if (type instanceof ListType && ((ListType) type).getSize().isPresent()) {
				return Collections.nCopies(((ListType) type).getSize().getAsInt(), "_").stream().collect(Collectors.joining(", ", "[", "]"));
			} else if (type instanceof TupleType) {
				return ((TupleType) type).getTypes().stream().map(t -> "_").collect(Collectors.joining(", ", "(", ")"));
			} else if (type instanceof ProductType) {
				ProductType product = (ProductType) type;
				if (product.getFields().isEmpty()) {
					return product.getName();
				} else {
					return product.getName() + product.getFields().stream().map(f -> "_").collect(Collectors.joining(", ", "(", ")"));
				}
			} else if (type instanceof SumType.VariantType) {
				SumType.VariantType variant = (SumType.VariantType) type;
				if (variant.getFields().isEmpty()) {
					return variant.getName();
				} else {
					return variant.getName() + variant.getFields().stream().map(f -> "_").collect(Collectors.joining(", ", "(", ")"));
				}
			} else {
				return "_";
			}
		}

		default String doApply(Space.Singleton space, boolean flattenList) {
			return "_";
		}

		default String doApply(Space.Product space, boolean flattenList) {
			Type type = space.type();
			if (type instanceof ListType) {
				if (flattenList) return space.spaces().stream().map(s -> doApply(s, flattenList)).collect(Collectors.joining(", "));
				else return space.spaces().stream().map(s -> doApply(s, true)).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ", "[", "]"));
			} else if (type instanceof TupleType) {
				if (flattenList) return space.spaces().stream().map(s -> doApply(s, flattenList)).collect(Collectors.joining(", "));
				else return space.spaces().stream().map(s -> doApply(s, true)).filter(s -> !s.isEmpty()).collect(Collectors.joining(", ", "(", ")"));
			} else if (type instanceof AlgebraicType) {
				String constructorStr = space.apply() instanceof SumType.VariantType ? ((SumType.VariantType) space.apply()).getName() : ((ProductType) space.apply()).getName();
				String parametersStr = space.spaces().stream().map(s -> doApply(s, true)).collect(Collectors.joining(", ", "(", ")"));
				return constructorStr + parametersStr;
			} else {
				return space.spaces().stream().map(s -> doApply(s, true)).collect(Collectors.joining(", ", "(", ")"));
			}
		}

		default String doApply(Space.Union space, boolean flattenList) {
			throw new RuntimeException("incorrect flatten result");
		}
	}

	static class Tuple<T, U> {

		private final T first;
		private final U second;

		private Tuple(T first, U second) {
			this.first = first;
			this.second = second;
		}

		public static <T, U> Tuple<T, U> of(T first, U second) {
			return new Tuple(first, second);
		}

		public T first() {
			return first;
		}

		public U second() {
			return second;
		}
	}

	static class ConstantType implements Type {

		private final boolean value;

		public ConstantType(boolean value) {
			this.value = value;
		}

		public boolean value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ConstantType that = (ConstantType) o;
			return value == that.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}
}
