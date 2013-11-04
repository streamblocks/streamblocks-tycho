package net.opendf.interp.values.predef;

import net.opendf.interp.Interpreter;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.exception.CALRuntimeException;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;


public class IntFunctions {
	public static class Undefined extends ArithOp {
		protected final int op(int a, int b) {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public int getNbrParameters() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	private static abstract class UnaryArithOp implements Function {
		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public int getNbrParameters() {
			return 1;
		}

		@Override
		public final Value copy() {
			return this;
		}
		
		@Override
		public final RefView apply(Interpreter interpreter) throws CALRuntimeException {
			Stack stack = interpreter.getStack();
			int a = conv.getInt(stack.pop());
			conv.setInt(stack.push(), op(a));
			return stack.pop();
		}
		
		protected abstract int op(int a);
	}

	public static class Negate extends UnaryArithOp {
		protected final int op(int a) {
			return -a;
		}
	}

	private static abstract class ArithOp implements Function {
		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public int getNbrParameters() {
			return 2;
		}

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) throws CALRuntimeException {
			Stack stack = interpreter.getStack();
			int b = conv.getInt(stack.pop());
			int a = conv.getInt(stack.pop());
			conv.setInt(stack.push(), op(a, b));
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

	public static class Div extends ArithOp {
		protected final int op(int a, int b) {
			return a / b;
		}
	}

	public static class Mod extends ArithOp {
		protected final int op(int a, int b) {
			return a % b;
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

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public int getNbrParameters() {
			return 2;
		}

		@Override
		public Value copy() {
			return this;
		}

		@Override
		public RefView apply(Interpreter interpreter) throws CALRuntimeException {
			Stack stack = interpreter.getStack();
			int value = conv.getInt(stack.pop());
			int size = conv.getInt(stack.pop());
			int sizeDiff = 32 - size;
			int result = value << sizeDiff >>> sizeDiff;
			conv.setInt(stack.push(), result);
			return stack.pop();
		}
	}

	private static abstract class CompOp implements Function {

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public int getNbrParameters() {
			return 2;
		}

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) throws CALRuntimeException {
			Stack stack = interpreter.getStack();
			int b = conv.getInt(stack.pop());
			int a = conv.getInt(stack.pop());
			conv.setBoolean(stack.push(), op(a, b));
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

	public static class NE extends CompOp {
		protected final boolean op(int a, int b) {
			return a != b;
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
