package net.opendf.transform;

import java.util.HashMap;
import java.util.Map;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.Variable;
import net.opendf.transform.util.AbstractActorMachineTransformer;

public class LocalVariableNumberTransformer extends
AbstractActorMachineTransformer<LocalVariableNumberTransformer.LookupTable> {

	public ActorMachine transformActorMachine(ActorMachine actorMachine){
		return transformActorMachine(actorMachine, new LocalVariableNumberTransformer.LookupTable());
	}

	public LookupTable lookupTable() {
		return new LookupTable();
	}

	@Override
	public Expression visitExprLet(ExprLet let, LookupTable table) {
		LookupTable frame = table.frame();
		int offset = 0;
		for (DeclVar decl : let.getVarDecls()) {
			frame.put(decl.getName(), offset++);
		}
		return super.visitExprLet(let, frame);
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, LookupTable table) {
		LookupTable frame = table.frame();
		int offset = 0;
		for (ParDeclValue par : lambda.getValueParameters()) {
			frame.put(par.getName(), offset++);
		}
		return super.visitExprLambda(lambda, frame);
	}

	@Override
	public Expression visitExprProc(ExprProc proc, LookupTable table) {
		LookupTable frame = table.frame();
		int offset = 0;
		for (ParDeclValue par : proc.getValueParameters()) {
			frame.put(par.getName(), offset++);
		}
		return super.visitExprProc(proc, frame);
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, LookupTable table) {
		LookupTable frame = table.frame();
		int offset = 0;
		for (DeclVar decl : block.getVarDecls()) {
			frame.put(decl.getName(), offset++);
		}
		return super.visitStmtBlock(block, table);
	}

	@Override
	public Variable transformVariable(Variable var, LookupTable table) {
		Variable result = table.get(var);
		return result == null ? var : result;
	}

	public static class LookupTable {
		private final Map<String, Integer> offsets;
		private final LookupTable parent;

		public LookupTable() {
			this(null);
		}

		private LookupTable(LookupTable parent) {
			this.parent = parent;
			offsets = new HashMap<>();
		}

		public LookupTable frame() {
			return new LookupTable(this);
		}

		public void put(String name, int offset) {
			offsets.put(name, offset);
		}

		public Variable get(Variable var) {
			if (var.hasLocation()) {
				return var;
			} else {
				return get(var.getName(), 0);
			}
		}

		private Variable get(String name, int nesting) {
			if (offsets.containsKey(name)) {
				return Variable.dynamicVariable(name, nesting, offsets.get(name));
			} else if (parent != null) {
				return parent.get(name, nesting + 1);
			} else {
				return null;
			}
		}

	}
}
