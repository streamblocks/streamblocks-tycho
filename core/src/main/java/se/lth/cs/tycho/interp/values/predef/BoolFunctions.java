package se.lth.cs.tycho.interp.values.predef;

import se.lth.cs.tycho.interp.Interpreter;
import se.lth.cs.tycho.interp.Stack;
import se.lth.cs.tycho.interp.TypeConverter;
import se.lth.cs.tycho.interp.values.Function;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.interp.values.Value;

public class BoolFunctions {

	private static abstract class LogicOp implements Function {

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
			Stack stack = interpreter.getStack();
			boolean b = conv.getBoolean(stack.pop());
			boolean a = conv.getBoolean(stack.pop());
			conv.setBoolean(stack.push(), op(a, b));
			return stack.pop();
		}

		protected abstract boolean op(boolean a, boolean b);
	}

	public static class And extends LogicOp {
		protected final boolean op(boolean a, boolean b) {
			return a && b;
		}
	}

	public static class Or extends LogicOp {
		protected final boolean op(boolean a, boolean b) {
			return a || b;
		}
	}

	public static class Not implements Function {

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
			Stack stack = interpreter.getStack();
			boolean b = conv.getBoolean(stack.pop());
			conv.setBoolean(stack.push(), !b);
			return stack.pop();
		}
	}

}
