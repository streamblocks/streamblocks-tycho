package net.opendf.interp.values.predef;

import net.opendf.interp.Interpreter;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;


public class IntFunctions {
	public static class Undefined extends ArithOp {
		protected final int op(int a, int b) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	private static abstract class ArithOp implements Function {
		public static Ref[] memory = new Ref[14];
		static{
			for(int i=0; i<memory.length; i++){
				memory[i] = new BasicRef();
			}
			memory[0].setValue(new Add());	
			memory[1].setValue(new Sub());	
			memory[2].setValue(new Mul());	
			memory[3].setValue(new BitAnd());	
			memory[4].setValue(new BitOr());	
			memory[5].setValue(new RightShiftSignExt());	
			memory[6].setValue(new LeftShift());	
			memory[7].setValue(new Truncate());	
			memory[8].setValue(new LT());	
			memory[9].setValue(new LE());	
			memory[10].setValue(new EQ());	
			memory[11].setValue(new NE());	
			memory[12].setValue(new GE());	
			memory[13].setValue(new GT());	
		}

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
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
		public Value copy() {
			return this;
		}

		@Override
		public RefView apply(Interpreter interpreter) {
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
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
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
