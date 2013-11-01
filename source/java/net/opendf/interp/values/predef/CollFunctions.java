package net.opendf.interp.values.predef;

import net.opendf.interp.Interpreter;
import net.opendf.interp.Stack;
import net.opendf.interp.TypeConverter;
import net.opendf.interp.exception.CALRuntimeException;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.List;
import net.opendf.interp.values.Range;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.Value;
import net.opendf.interp.values.Iterator;

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
			if(function.getNbrParameters() != 2){
				throw new CALRuntimeException("The function passed to accumulate() must take two arguments. Found " + function.getNbrParameters() + " argument function.");
			}
			while(!iter.finished()){
				accValue.assignTo(stack.push());
				iter.assignTo(stack.push());
				accValue = function.apply(interpreter);
				iter.advance();
			}
			return accValue;
		}

		@Override
		public int getNbrParameters() {
			return 3;
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

		@Override
		public int getNbrParameters() {
			return 2;
		}
	}

}
