package net.opendf.interp.values.predef;

import net.opendf.interp.Simulator;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;

public class IntFunctions {
	private static abstract class ArithOp implements Function {

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			TypeConverter conv = sim.converter();
			int b = conv.getInt(stack.pop());
			int a = conv.getInt(stack.pop());
			Ref r = stack.push();
			conv.setInt(r, op(a, b));
			return stack.pop();
		}

		protected abstract int op(int a, int b);
	}

	public static class Add extends ArithOp {
		protected final int op(int a, int b) {
			return a + b;
		}
	}

	public static class Sub extends ArithOp {
		protected final int op(int a, int b) {
			return a - b;
		}
	}

	public static class Mul extends ArithOp {
		protected final int op(int a, int b) {
			return a * b;
		}
	}

	public static class BitAnd extends ArithOp {
		protected final int op(int a, int b) {
			return a & b;
		}
	}

	public static class BitOr extends ArithOp {
		protected final int op(int a, int b) {
			return a | b;
		}
	}

	public static class RightShiftSignExt extends ArithOp {
		protected final int op(int a, int b) {
			return a >>> b;
		}
	}

	public static class LeftShift extends ArithOp {
		protected final int op(int a, int b) {
			return a << b;
		}
	}

	public static class Truncate implements Function {

		@Override
		public Value copy() {
			return this;
		}

		@Override
		public RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			TypeConverter conv = sim.converter();
			int value = conv.getInt(stack.pop());
			int size = conv.getInt(stack.pop());
			int sizeDiff = 32 - size;
			int result = value << sizeDiff >>> sizeDiff;
			conv.setInt(stack.push(), result);
			return stack.pop();
		}
	}

	private static abstract class CompOp implements Function {

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			TypeConverter conv = sim.converter();
			int b = conv.getInt(stack.pop());
			int a = conv.getInt(stack.pop());
			Ref r = stack.push();
			conv.setBoolean(r, op(a, b));
			return stack.pop();
		}

		protected abstract boolean op(int a, int b);
	}

	public static class LT extends CompOp {
		protected final boolean op(int a, int b) {
			return a < b;
		}
	}

	public static class LE extends CompOp {
		protected final boolean op(int a, int b) {
			return a <= b;
		}
	}

	public static class EQ extends CompOp {
		protected final boolean op(int a, int b) {
			return a == b;
		}
	}

	public static class GE extends CompOp {
		protected final boolean op(int a, int b) {
			return a >= b;
		}
	}

	public static class GT extends CompOp {
		protected final boolean op(int a, int b) {
			return a > b;
		}
	}

}
