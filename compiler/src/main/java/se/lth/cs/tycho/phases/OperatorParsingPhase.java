package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperatorParsingPhase implements Phase {
	@Override
	public String getDescription() {
		return "Parses binary operation sequences to binary operations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.withSourceUnits(task.getSourceUnits().stream().map(unit -> {
			Helper helper = new Helper(unit, context.getReporter(), orccOperators);
			Transformation transformation = new Transformation(helper);
			return unit.transformChildren(transformation);
		}).collect(Collectors.toList()));
	}

	private static final class Transformation implements Function<IRNode, IRNode> {
		private final Helper helper;

		public Transformation(Helper helper) {
			this.helper = helper;
		}

		@Override
		public IRNode apply(IRNode node) {
			IRNode transformed = node.transformChildren(this);
			if (transformed instanceof ExprBinaryOp) {
				ExprBinaryOp binaryOp = (ExprBinaryOp) transformed;
				helper.checkOperators(binaryOp);
				if (binaryOp.getOperations().size() > 1) {
					return helper.shuntingYard(binaryOp);
				}
			}
			return transformed;
		}
	}

	private static final class Helper {
		private final SourceUnit sourceUnit;
		private final Reporter reporter;
		private final Map<String, BinaryOperator> operatorMap;

		public Helper(SourceUnit sourceLocation, Reporter reporter, Map<String, BinaryOperator> operatorMap) {
			this.sourceUnit = sourceLocation;
			this.reporter = reporter;
			this.operatorMap = operatorMap;
		}

		private void checkOperators(ExprBinaryOp binaryOp) {
//			binaryOp.getOperations().stream()
//					.forEach(op -> reporter.report(new Diagnostic(Diagnostic.Kind.INFO, "Encountered operator: " + op, sourceUnit, binaryOp)));
			binaryOp.getOperations().stream()
					.filter(op -> !operatorMap.containsKey(op))
					.forEach(op -> reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Unknown operator: " + op, sourceUnit, binaryOp)));
		}

		private Expression shuntingYard(ExprBinaryOp binaryOp) {
			List<String> operations = binaryOp.getOperations();
			List<Expression> operands = binaryOp.getOperands();
			LinkedList<Expression> out = new LinkedList<>();
			LinkedList<String> ops = new LinkedList<>();
			int i = 0;
			out.add(operands.get(i));
			while (i < operations.size()) {
				int prec = operatorMap.get(operations.get(i)).precedence;
				boolean rightAssoc = operatorMap.get(operations.get(i)).rightAssociative;
				while (!ops.isEmpty() && (!rightAssoc && prec <= operatorMap.get(ops.getLast()).precedence || rightAssoc && prec < operatorMap.get(ops.getLast()).precedence)) {
					transformOperator(out, ops);
				}
				ops.addLast(operations.get(i));
				i += 1;
				out.add(operands.get(i));
			}
			while (!ops.isEmpty()) {
				transformOperator(out, ops);
			}
			assert out.size() == 1;
			return out.getFirst();
		}

		private void transformOperator(LinkedList<Expression> out, LinkedList<String> ops) {
			String operator = ops.removeLast();
			Expression right = out.removeLast();
			Expression left = out.removeLast();
			out.add(new ExprBinaryOp(ImmutableList.of(operator), ImmutableList.of(left, right)));
		}
	}



	private static class BinaryOperator {
		private final String name;
		private final int precedence;
		private final boolean rightAssociative;

		public BinaryOperator(String name, int precedence, boolean rightAssociative) {
			this.name = name;
			this.precedence = precedence;
			this.rightAssociative = rightAssociative;
		}
	}

	private static final Map<String, BinaryOperator> orccOperators = Stream.of(
			new BinaryOperator("..", -1, false),
			new BinaryOperator("&", 0, false),
			new BinaryOperator("|", 1, false),
			new BinaryOperator("^", 2, false),
			new BinaryOperator("/", 3, false),
			new BinaryOperator("div", 4, false),
			new BinaryOperator("==", 5, false),
			new BinaryOperator("=", 5, false),
			new BinaryOperator("**", 6, true),
			new BinaryOperator(">=", 7, false),
			new BinaryOperator(">", 8, false),
			new BinaryOperator("<=", 9, false),
			new BinaryOperator("&&", 10, false),
			new BinaryOperator("and", 10, false),
			new BinaryOperator("||", 11, false),
			new BinaryOperator("or", 11, false),
			new BinaryOperator("<", 12, false),
			new BinaryOperator("-", 13, false),
			new BinaryOperator("%", 14, false),
			new BinaryOperator("mod", 14, false),
			new BinaryOperator("!=", 15, false),
			new BinaryOperator("+", 16, false),
			new BinaryOperator("<<", 17, false),
			new BinaryOperator(">>", 18, false),
			new BinaryOperator("*", 19, false)
	).collect(Collectors.toMap(op -> op.name, Function.identity()));
}
