package net.opendf.interp;

import net.opendf.interp.values.Collection;
import net.opendf.interp.values.Iterator;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.util.ImmutableList;

public class GeneratorFilterHelper {

	private final Interpreter interpreter;
	private final Stack stack;
	private final TypeConverter converter;

	public GeneratorFilterHelper(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.stack = interpreter.getStack();
		this.converter = TypeConverter.getInstance();
	}

	public void generate(ImmutableList<GeneratorFilter> generators, Runnable action, Environment env) {
		if (generators == null || generators.isEmpty()){
			action.run();
		} else {
			generate(generators, action, env, 0, 0, null);
		}
 	}
 
	// NOTE, if you change here you must also change in MemoryLayoutTransformer so the stack offsets are correct!!!
	private void generate(ImmutableList<GeneratorFilter> generators, Runnable action, Environment env, int gen, int var, Collection coll) {
		assert !generators.isEmpty();
		if (gen == generators.size()) {
			// all generators have given their variables values, run the body for this generator
			action.run();
		} else if (var == generators.get(gen).getVariables().size()) {
			// all local variables has been given a value, check if this passes the filters
			boolean included = true;
			for (Expression filter : generators.get(gen).getFilters()) {
				if (!converter.getBoolean(interpreter.evaluate(filter, env))) {
					included = false;
					break;
				}
			}
			if (included){
				generate(generators, action, env, gen + 1, 0, null);
			}
		} else {
			if(coll==null){
				// The collection is evaluated one time for each generator. This must be done before any local variables are pushed to the stack.
				RefView c = interpreter.evaluate(generators.get(gen).getCollectionExpr(), env);
				coll = converter.getCollection(c);
			}
			Iterator iter = coll.iterator();
			while (!iter.finished()) {
				stack.push(iter);
				generate(generators, action, env, gen, var + 1, coll);
				stack.pop();
				iter.advance();
			}
		}
	}
}
