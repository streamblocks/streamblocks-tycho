package net.opendf.interp.values.predef;

import net.opendf.interp.Simulator;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;

public class BoolFunctions {
	private static abstract class LogicOp implements Function {

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			TypeConverter conv = sim.converter();
			boolean b = conv.getBoolean(stack.pop());
			boolean a = conv.getBoolean(stack.pop());
			Ref r = stack.push();
			conv.setBoolean(r, op(a, b));
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

}
