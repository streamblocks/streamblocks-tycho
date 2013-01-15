package net.opendf.interp.values;

import net.opendf.interp.Simulator;
import net.opendf.interp.Stack;

public class IntegerFunctions {
	private static long mask(int bits) {
		assert bits > 0 && bits < 64;
		return (1L << bits) - 1;
	}
	
	private static abstract class BinOp implements Function {
		protected final long bitmask;
		
		public BinOp(int bits) {
			bitmask = mask(bits);
		}

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			long b = stack.pop().getLong();
			long a = stack.pop().getLong();
			Ref r = stack.push();
			r.setLong(op(a, b));
			return stack.pop();
		}
		
		protected abstract long op(long a, long b);
	}
	
	public static class Add extends BinOp {
		public Add(int bits) {
			super(bits);
		}
		
		protected final long op(long a, long b) {
			return (a + b) & bitmask;
		}
	}

	public static class Mul extends BinOp {
		public Mul(int bits) {
			super(bits);
		}
		
		protected final long op(long a, long b) {
			return (a * b) & bitmask;
		}
	}

}
