package net.opendf.interp.values.predef;

import net.opendf.interp.Interpreter;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.Range;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;

public class CollFunctions {
	public static class IntegerRange implements Function {

		private TypeConverter conv = TypeConverter.getInstance();

		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
			Stack stack = interpreter.getStack();
			int to = conv.getInt(stack.pop());
			int from = conv.getInt(stack.pop());
			conv.setCollection(stack.push(), new Range(from, to));
			return stack.pop();
		}
	}

}
