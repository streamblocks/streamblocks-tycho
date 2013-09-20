package net.opendf.interp;

import java.util.BitSet;

import net.opendf.interp.exception.CALIndexOutOfBoundsException;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Ref;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public class BasicMemory implements Memory {
	
	private final Ref[][] mem;               // mem[scopeID][offset]
	private final BitSet[] inClosure;        // set to true if the corresponding memory cell is part of a closure
	
	public BasicMemory(ActorMachine actorMachine) {
		ImmutableList<ImmutableList<DeclVar>> scopes = actorMachine.getScopes();
		int nbrScopes = scopes.size();
		mem = new BasicRef[nbrScopes][];
		inClosure = new BitSet[nbrScopes];
		for (int i = 0; i < nbrScopes; i++) {
			int scopeSize = scopes.get(i).size();
			mem[i] =  new BasicRef[scopeSize];
			inClosure[i] = new BitSet(scopeSize);
			for(int j=0; j<scopeSize; j++){
				mem[i][j] = new BasicRef();
			}
		}
	}
	private BasicMemory(int size) {
		mem = new BasicRef[1][];
		mem[0] = new BasicRef[size];
		inClosure = new BitSet[1];
		inClosure[0] = new BitSet(size);
	}
	
	@Override
	public Ref get(Variable var) {
		try{
			assert var.isStatic();
			return mem[var.getScope()][var.getOffset()];
		} catch(java.lang.ArrayIndexOutOfBoundsException e){
			String scopeMsg = var.getScope()<0 || mem.length<=var.getScope() ? " the scope does not exist" : ", size of scope: " + mem[var.getScope()].length;
			throw new CALIndexOutOfBoundsException("access to static memory, scope: " + var.getScope() + ", offset: " + var.getOffset() + scopeMsg);
		}
	}

	@Override
	public Ref declare(int scope, int offset) {
		if (inClosure[scope].get(offset)) {
			mem[scope][offset] = new BasicRef();
			inClosure[scope].clear(offset);
		}
		return mem[scope][offset];
	}

	@Override
	public BasicMemory closure(ImmutableList<Variable> variables, Stack stack) {
		BasicMemory newClosure = new BasicMemory(variables.size());
		for(int i=0; i<variables.size(); i++){
			Variable v = variables.get(i);
			if(v.isDynamic()){
				newClosure.mem[0][i] = stack.closure(v.getOffset());
				//TODO, copy from stack
			} else {
				newClosure.mem[0][i] = mem[v.getScope()][v.getOffset()];
				inClosure[v.getScope()].set(v.getOffset());
			}
		}
		return newClosure;
	}

}
