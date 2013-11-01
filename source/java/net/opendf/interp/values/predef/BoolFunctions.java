package net.opendf.interp.values.predef;

import net.opendf.interp.Interpreter;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;

public class BoolFunctions {

	private static abstract class BinaryLogicOp implements Function {

		@Override
		public int getNbrParameters() {
			return 2;
		}

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

	public static class And extends BinaryLogicOp {
		protected final boolean op(boolean a, boolean b) {
			return a && b;
		}

	}

	public static class Or extends BinaryLogicOp {
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

		@Override
		public int getNbrParameters() {
			return 1;
		}
	}

}
