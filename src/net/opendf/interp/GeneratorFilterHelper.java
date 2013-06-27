package net.opendf.interp;

import net.opendf.interp.values.Collection;
import net.opendf.interp.values.Iterator;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;

public class GeneratorFilterHelper {

	private final Interpreter interpreter;
	private final Stack stack;
	private final TypeConverter converter;

	public GeneratorFilterHelper(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.stack = interpreter.getStack();
		this.converter = TypeConverter.getInstance();
	}

	public void generate(GeneratorFilter[] generators, Runnable action, Environment env) {
		generate(generators, action, env, 0, 0);
	}

	private void generate(GeneratorFilter[] generators, Runnable action, Environment env, int gen, int var) {
		if (generators == null || gen == generators.length) {
			action.run();
		} else if (var == generators[gen].getVariables().length) {
			generate(generators, action, env, gen + 1, 0);
		} else {
			RefView c = interpreter.evaluate(generators[gen].getCollectionExpr(), env);
			Collection coll = converter.getCollection(c);
			Iterator iter = coll.iterator();
			while (!iter.finished()) {
				stack.push(iter);
				boolean included = true;
				for (Expression filter : generators[gen].getFilters()) {
					if (!converter.getBoolean(interpreter.evaluate(filter, env))) {
						included = false;
						break;
					}
				}
				if (included)
					generate(generators, action, env, gen, var + 1);
				stack.pop();
				iter.advance();
			}
		}
	}
}
