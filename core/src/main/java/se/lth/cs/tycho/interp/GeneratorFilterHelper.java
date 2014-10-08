package se.lth.cs.tycho.interp;

import se.lth.cs.tycho.interp.preprocess.VariableOffsetTransformer;
import se.lth.cs.tycho.interp.preprocess.VariableOffsetTransformer.LookupTable;
import se.lth.cs.tycho.interp.values.Collection;
import se.lth.cs.tycho.interp.values.Iterator;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class GeneratorFilterHelper {

	private final Interpreter interpreter;
	private final Stack stack;
	private final TypeConverter converter;

	public GeneratorFilterHelper(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.stack = interpreter.getStack();
		this.converter = TypeConverter.getInstance();
	}

	/**
	 * Called during the interpretation of an actor machine.
	 * Called by {@link ExpressionEvaluator} and {@link StatementExecutor}.
	 * NOTE, the stack must be modified in the same order by both interpret() and memoryLayout()
	 * 
	 * @param generators
	 * @param action
	 * @param env
	 */
	public void interpret(ImmutableList<GeneratorFilter> generators, Runnable action, Environment env) {
		if (generators == null || generators.isEmpty()){
			action.run();
		} else {
			interpret(generators, action, env, 0, 0, null);
		}
 	}
 
	private void interpret(ImmutableList<GeneratorFilter> generators, Runnable action, Environment env, int gen, int var, Collection coll) {
		assert !generators.isEmpty();
		if (gen == generators.size()) {
			// all generators have given their variables values, run the body
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
				interpret(generators, action, env, gen + 1, 0, null);
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
				interpret(generators, action, env, gen, var + 1, coll);
				stack.pop();
				iter.advance();
			}
		}
	}

	/**
	 * Called when an actor machine is prepared for interpretation.
	 * The purpose is to find the offset on the stack for all variables.
	 * Called by {@link VariableOffsetTransformer}.
	 * NOTE, the stack must be modified in the same order by both interpret() and setVariableOffsets()
	 * 
	 * @param generators
	 * @param action
	 * @param lookupTable
	 * @param transformer - The run() method transform the content of this generator, i.e. the list of elements for list generators, the statement body for foreach statement
	 *                      run() is called after the names introduced by this generator has been added to the lookup table
	 * @return the transformed {@link GeneratorFilter}
	 */
	public static ImmutableList<GeneratorFilter> setVariableOffsets(ImmutableList<GeneratorFilter> generators, Runnable action, 
			final LookupTable lookupTable, VariableOffsetTransformer transformer){
		ImmutableList.Builder<GeneratorFilter> generatorBuilder = ImmutableList.builder();
		for(GeneratorFilter gen : generators){
			// first evaluate all values this generator should iterate over. At this point the local names are not visible
			Expression collectionExpr = transformer.transformExpression(gen.getCollectionExpr(), lookupTable);
			// introduce all local names by pushing the values to the stack
			for(LocalVarDecl decl : gen.getVariables()){
				assert decl.getInitialValue() == null;  // initial value is not allowed, the generator creates the values
				lookupTable.addName(decl.getName());
			}
			// evaluate the filters
			ImmutableList.Builder<Expression> filterBuilder = ImmutableList.builder();
			for(Expression filter : gen.getFilters()){
				filterBuilder.add(transformer.transformExpression(filter, lookupTable));
			}
			// now all offsets for named accesses in this generator has been computed
			generatorBuilder.add(gen.copy(gen.getVariables(), collectionExpr, filterBuilder.build()));
		}

		action.run();

		// pop the local names from the stack
		for(GeneratorFilter gen : generators){
			for(int i=gen.getVariables().size(); i>0; i--){
				lookupTable.pop();
			}
		}
		return generatorBuilder.build();
	}

}
