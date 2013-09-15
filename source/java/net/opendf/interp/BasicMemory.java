package net.opendf.interp;

import java.util.BitSet;

import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Ref;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public class BasicMemory implements Memory {
	
	private final Ref[][] mem;               // mem[scopeID][offset]
	private final BitSet[] closure;          // set to true if the corresponding memory cell is part of a closure
	
	public BasicMemory(ActorMachine actorMachine) {
		ImmutableList<ImmutableList<DeclVar>> scopes = actorMachine.getScopes();
		int nbrScopes = scopes.size();
		mem = new BasicRef[nbrScopes][];
		closure = new BitSet[nbrScopes];
		for (int i = 0; i < nbrScopes; i++) {
			int scopeSize = scopes.get(i).size();
			mem[i] =  new BasicRef[scopeSize];
			closure[i] = new BitSet(scopeSize);
			for(int j=0; j<scopeSize; j++){
				mem[i][j] = new BasicRef();
			}
		}
	}
	
	@Override
	public Ref get(Variable var) {
		assert var.isStatic();
		return mem[var.getScope()][var.getOffset()];
	}

	@Override
	public Ref declare(int scope, int offset) {
		if (closure[scope].get(offset)) {
			mem[scope][offset] = new BasicRef();
			closure[scope].clear(offset);
		}
		return mem[scope][offset];
	}

	@Override
	public BasicMemory closure(int[] select, Ref[] add) {
		/*
		Ref[] c = new Ref[select.length + add.length];
		int i = 0;
		for (int index : select) {
			c[i++] = mem[index];
			closure.set(index);
		}
		for (Ref r : add) {
			c[i++] = r;
		}
		return new BasicMemory(c);
		*/
		//FIXME, closure
		return this;
	}

}
