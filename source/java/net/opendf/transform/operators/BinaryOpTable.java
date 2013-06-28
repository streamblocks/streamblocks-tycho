package net.opendf.transform.operators;

import java.util.HashMap;
import java.util.Map;

public class BinaryOpTable {
	private final Map<String, Operator> table;

	public BinaryOpTable() {
		this.table = new HashMap<>();
	}

	public void add(Operator operator) {
		table.put(operator.getOperator(), operator);
	}

	public Operator get(String operator) {
		return table.get(operator);
	}

	public static class Operator {
		private final String operator;
		private final String function;
		private final int precedence;

		public Operator(String operator, String function, int precedence) {
			this.operator = operator;
			this.function = function;
			this.precedence = precedence;
		}

		public String getOperator() {
			return operator;
		}
		
		public String getFunction() {
			return function;
		}

		public int getPrecedence() {
			return precedence;
		}
	}

}
