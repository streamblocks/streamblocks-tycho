package net.opendf.interp.values.predef;

import net.opendf.interp.Simulator;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.Range;
import net.opendf.interp.values.Ref;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;

public class CollFunctions {
	public static class IntegerRange implements Function {

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(int args, Simulator sim) {
			assert args == 2;
			Stack stack = sim.stack();
			TypeConverter conv = sim.converter();
			int to = conv.getInt(stack.pop());
			int from = conv.getInt(stack.pop());
			Ref r = stack.push();
			conv.setCollection(r, new Range(from, to));
			return stack.pop();
		}
	}

}
