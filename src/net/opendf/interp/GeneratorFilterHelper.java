package net.opendf.interp;

import net.opendf.interp.values.Collection;
import net.opendf.interp.values.Iterator;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;

public class GeneratorFilterHelper {
	
	protected final Simulator simulator;
	
	public GeneratorFilterHelper(Simulator simulator) {
		this.simulator = simulator;
	}

	public void generate(GeneratorFilter[] generators, Runnable action, Environment env) {
		generate(generators, action, env, 0, 0);
	}
	
	private void generate(GeneratorFilter[] generators, Runnable action, Environment env, int gen, int var) {
		Stack stack = simulator.stack();
		Evaluator evaluator = simulator.evaluator();
		TypeConverter converter = simulator.converter();
		if (generators == null || gen == generators.length) {
			action.run();
		} else if (var == generators[gen].getVariables().length) {
			generate(generators, action, env, gen+1, 0);
		} else {
			RefView c = evaluator.evaluate(generators[gen].getCollectionExpr(), env);
			Collection coll = converter.getCollection(c);
			Iterator iter = coll.iterator();
			while (!iter.finished()) {
				stack.push(iter);
				boolean included = true;
				for (Expression filter : generators[gen].getFilters()) {
					if (!converter.getBoolean(evaluator.evaluate(filter, env))) {
						included = false;
						break;
					}
				}
				if (included) generate(generators, action, env, gen, var+1);
				stack.pop();
				iter.advance();
			}
		}
	}

	
}
