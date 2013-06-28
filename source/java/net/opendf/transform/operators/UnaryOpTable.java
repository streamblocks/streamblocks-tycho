package net.opendf.transform.operators;

import java.util.HashMap;
import java.util.Map;

public class UnaryOpTable {
	private final Map<String, Operator> table;
	
	public UnaryOpTable() {
		table = new HashMap<>();
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
		public Operator(String operator, String function) {
			this.operator = operator;
			this.function = function;
		}
		public String getOperator() {
			return operator;
		}
		public String getFunction() {
			return function;
		}
	}

}
