package net.opendf.interp;

import net.opendf.interp.attributed.AttrExprLiteral;
import net.opendf.interp.attributed.AttrExprVariable;
import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.Iterator;
import net.opendf.interp.values.List;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.GeneratorFilter;

public class Test {
	
	private static AttrExprLiteral intLit(int value) {
		return new AttrExprLiteral(new ExprLiteral(ExprLiteral.litInteger, Integer.toString(value)), ConstRef.of(value));
	}
	
	private static AttrExprVariable var(String name, int pos, boolean onStack) {
		return new AttrExprVariable(new ExprVariable(name), pos, onStack);
	}
	
	private static GeneratorFilter gen(String var, Expression[] elems) {
		DeclVar[] vars = new DeclVar[] { new DeclVar(null, var, null) };
		ExprList list = new ExprList(elems);
		return new GeneratorFilter(vars, list, new Expression[0]);
	}
	
	public static void main(String[] args) {
		TypeConverter conv = new TypeConverter();
		
		Environment env = new BasicEnvironment(new Channel[0], 0);
		Simulator sim = new Sim(2);
		Evaluator eval = sim.evaluator();
		
		Expression[] elems = new Expression[] { var("x", 1, true), var("y", 0, true) };
		GeneratorFilter[] gens = new GeneratorFilter[] {
			gen("x", new Expression[] {intLit(1), intLit(2), intLit(3)}),
			gen("y", new Expression[] {intLit(5), intLit(6)})
		};
		ExprList exprList = new ExprList(elems, gens);
		
		RefView r = null;
		final long repetitions = 10000000L;
		long start = System.nanoTime();
		for (long i = 0; i < repetitions; i++) {
			r = eval.evaluate(exprList, env);
		}
		long time = System.nanoTime() - start;
		List list = conv.getList(r);
		Iterator iter = list.iterator();
		System.out.print("[ ");
		while (!iter.finished()) {
			System.out.print(conv.getInt(iter) + " ");
			iter.advance();
		}
		System.out.println("]");
		System.out.println(time/repetitions + " ns");
	}
}
