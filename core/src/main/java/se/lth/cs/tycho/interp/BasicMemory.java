package se.lth.cs.tycho.interp;

import java.util.BitSet;

import se.lth.cs.tycho.interp.exception.CALIndexOutOfBoundsException;
import se.lth.cs.tycho.interp.values.BasicRef;
import se.lth.cs.tycho.interp.values.Ref;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.util.ImmutableList;

public class BasicMemory implements Memory {
	
	private final Ref[][] mem;               // mem[scopeID][offset]
	private final BitSet[] inClosure;        // set to true if the corresponding memory cell is part of a closure
	
	public BasicMemory(ActorMachine actorMachine) {
		ImmutableList<Scope> scopes = actorMachine.getScopes();
		int nbrScopes = scopes.size();
		mem = new BasicRef[nbrScopes][];
		inClosure = new BitSet[nbrScopes];
		for (int i = 0; i < nbrScopes; i++) {
			int scopeSize = scopes.get(i).getDeclarations().size();
			mem[i] =  new BasicRef[scopeSize];
			inClosure[i] = new BitSet(scopeSize);
			for(int j=0; j<scopeSize; j++){
				mem[i][j] = new BasicRef();
			}
		}
	}
	public BasicMemory(int[] sizes) {
		mem = new BasicRef[sizes.length][];
		inClosure = new BitSet[sizes.length];
		for(int scopeId=0; scopeId<sizes.length; scopeId++){
			mem[scopeId] = new BasicRef[sizes[scopeId]];
			for(int j=0; j<sizes[scopeId]; j++){
				mem[scopeId][j] = new BasicRef();
			}
			inClosure[scopeId] = new BitSet(sizes[scopeId]);
		}
	}
	
	@Override
	public Ref get(VariableLocation var) {
		try{
			assert var.isScopeVariable();
			return mem[var.getScopeId()][var.getOffset()];
		} catch(java.lang.ArrayIndexOutOfBoundsException e){
			String scopeMsg = var.getScopeId()<0 || mem.length<=var.getScopeId() ? " the scope does not exist" : ", size of scope: " + mem[var.getScopeId()].length;
			throw new CALIndexOutOfBoundsException("access to static memory, scope: " + var.getScopeId() + ", offset: " + var.getOffset() + scopeMsg);
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
		int[] config = new int[1];
		config[0] = variables.size();
		BasicMemory newClosure = new BasicMemory(config);
		for(int i=0; i<variables.size(); i++){
			VariableLocation v = (VariableLocation)variables.get(i);
			if(v.isScopeVariable()){
				newClosure.mem[0][i] = mem[v.getScopeId()][v.getOffset()];
				inClosure[v.getScopeId()].set(v.getOffset());
			} else {
				newClosure.mem[0][i] = stack.closure(v.getOffset());
			}
		}
		return newClosure;
	}

}
