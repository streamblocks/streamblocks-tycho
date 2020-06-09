package se.lth.cs.tycho.meta.interp.op.operator;

public abstract class Operator {

	public static Operator of(String op) {
		switch (op) {
			case "!":
			case "not":
				return new OperatorNot();
			case "#":
				return new OperatorSize();
			case "+":
				return new OperatorPlus();
			case "-":
				return new OperatorMinus();
			case "*":
				return new OperatorTimes();
			case "/":
			case "div":
				return new OperatorDiv();
			case "%":
			case "mod":
				return new OperatorMod();
			case "||":
				return new OperatorDisjunction();
			case "&&":
				return new OperatorConjunction();
			case "|":
				return new OperatorOr();
			case "&":
				return new OperatorAnd();
			case "^":
				return new OperatorXOr();
			case "<<":
				return new OperatorShiftLeft();
			case ">>":
				return new OperatorShiftRight();
			case "=":
				return new OperatorEqual();
			case "!=":
				return new OperatorDifferent();
			case "<":
				return new OperatorLowerThan();
			case "<=":
				return new OperatorLowerEqualThan();
			case ">":
				return new OperatorGreaterThan();
			case ">=":
				return new OperatorGreaterEqualThan();
			case "in":
				return new OperatorIn();
			default:
				throw new RuntimeException("Unsupported operator " + op + ".");
		}
	}
}