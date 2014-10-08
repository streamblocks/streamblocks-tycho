package se.lth.cs.tycho.interp.values.predef;

import se.lth.cs.tycho.interp.Interpreter;
import se.lth.cs.tycho.interp.Stack;
import se.lth.cs.tycho.interp.TypeConverter;
import se.lth.cs.tycho.interp.values.Function;
import se.lth.cs.tycho.interp.values.Iterator;
import se.lth.cs.tycho.interp.values.List;
import se.lth.cs.tycho.interp.values.Range;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.interp.values.Value;

public class CollFunctions {
	public static class ListAccumulate implements Function {
		private TypeConverter conv = TypeConverter.getInstance();
		@Override
		public final Value copy() {
			return this;
		}

		@Override
		public final RefView apply(Interpreter interpreter) {
			Stack stack = interpreter.getStack();
			List list = conv.getList(stack.pop());
			RefView accValue = stack.pop();
			Function function = conv.getFunction(stack.pop());
			Iterator iter = list.iterator();
			while(!iter.finished()){
				accValue.assignTo(stack.push());
				iter.assignTo(stack.push());
				accValue = function.apply(interpreter);
				iter.advance();
			}
			return accValue;
		}
	}


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
