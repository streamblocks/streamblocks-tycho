package net.opendf.analyze.memory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import net.opendf.interp.exception.CALRuntimeException;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractActorTransformer;

/**
 * This class computes the set of free variables for any expression.
 * The set of free variables are cached in the ExprLambda and ExprProc classes.
 * 
 * It also orders variable declarations to a valid initialization order.
 * Cyclic dependencies in variable initialization are detected.
 * This is done by transformVarDecls(ImmutableList<DeclVar> varDecls, Set<String> c).
 */

public class FreeVariablesTransformer extends AbstractActorTransformer<Set<String>> {

	public static Actor transformActor(Actor actor){
		FreeVariablesTransformer t = new FreeVariablesTransformer();
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		Actor a = t.transformActor(actor, c);
		return a;
	}

	@Override
	public Variable transformVariable(Variable var, Set<String> c) {
		c.add(var.getName());
		return var;
	}

	@Override
	public Expression visitExprLambda(ExprLambda lambda, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			Expression body = lambda.getBody().accept(this, freeVars);
			for(ParDeclValue v :lambda.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.namedVariable(name));
			}
			c.addAll(freeVars);
			return lambda.copy(lambda.getTypeParameters(), lambda.getValueParameters(), body, lambda.getReturnType(), 
					builder.build(), true);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Expression visitExprLet(ExprLet let, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			ImmutableList<DeclVar> newDecls = transformVarDecls(let.getVarDecls(), freeVars);

			Expression body = let.getBody().accept(this, freeVars);
			// remove the locally declared names
			for(DeclVar v :let.getVarDecls()){
				freeVars.remove(v.getName());
			}
			c.addAll(freeVars);
			return let.copy(let.getTypeDecls(), newDecls, body);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Statement visitStmtBlock(StmtBlock block, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			ImmutableList<DeclVar> newDecls = transformVarDecls(block.getVarDecls(), freeVars);

			ImmutableList.Builder<Statement> bodyBuilder = new ImmutableList.Builder<Statement>();
			for(Statement stmt : block.getStatements()){
				bodyBuilder.add(stmt.accept(this, freeVars));
			}
			c.addAll(freeVars);
			return block.copy(block.getTypeDecls(), newDecls, bodyBuilder.build());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Expression visitExprProc(ExprProc proc, Set<String> c) {
		try {
			Set<String> freeVars = c.getClass().newInstance();
			Statement body = transformStatement(proc.getBody(), freeVars);
			for(ParDeclValue v : proc.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.namedVariable(name));
			}
			c.addAll(freeVars);
			return proc.copy(proc.getTypeParameters(), proc.getValueParameters(), body, builder.build(), true);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	

	private class DeclVarComparator implements Comparator<DeclVar>{
		// for performance reason, cache the set of free variables
		HashMap<String, Set<String>> freeVars;
		DeclVarComparator(HashMap<String, Set<String>> freeVars){
			this.freeVars = freeVars;
		}
		@Override
		public int compare(DeclVar o1, DeclVar o2) {
			String o1Name = o1.getName();
			String o2Name = o2.getName();
			Set<String> o1FreeVars = freeVars.get(o1Name);
			Set<String> o2FreeVars = freeVars.get(o2Name);
			assert o1FreeVars!=null && o2FreeVars!=null;
			
			if(o1FreeVars.contains(o2Name)){
				// o1 depends on o2
				/* this only detects cyclic dependencies where the cycle is of size 2
				if(o2FreeVars.contains(o1Name)){
					throw new CALRuntimeException("cyclic dependency when initializing variables. Names: " + o1Name + " and " + o2Name);
				}
				*/
				return 1; // o1 > o2
			} else {
				if(o2FreeVars.contains(o1Name)){
					// o2 depends on o1
					return -1;
				}
			}
			return o1Name.compareTo(o2Name);  // alphabetic order for unrelated variables
		}
		
	}
	@Override
	public ImmutableList<DeclVar> transformVarDecls(ImmutableList<DeclVar> varDecls, Set<String> c) {
		try {
			Set<String> allFreeVars = c.getClass().newInstance();
			int size = varDecls.size();
			HashMap<String, Set<String>> freeVars = new HashMap();
			DeclVar[] newDecls = new DeclVar[size];
			// compute the free variables for each declaration
			for(int i=0; i<size; i++){
				Set<String> uses = c.getClass().newInstance();
				allFreeVars.addAll(uses);
				newDecls[i] = transformVarDecl(varDecls.get(i), uses);
				freeVars.put(varDecls.get(i).getName(), uses);				
			}
			// sort the declarations in initialization order
			DeclVarComparator cmp = new DeclVarComparator(freeVars);
			Arrays.sort(newDecls, cmp);
			// check for cyclic dependencies.
			for(int i=0; i<newDecls.length; i++){
				Set<String> dependsOn = freeVars.get(newDecls[i].getName());
				// check if this variable is depending on any variable initialized later
				for(int j=i; j<newDecls.length; j++){
					if(dependsOn.contains(newDecls[j].getName())){
						throw new CALRuntimeException("cyclic dependency when initializing variables. Dependent variables: " 
					                                  + newDecls[i].getName() + " and " + newDecls[j].getName());						
					}
				}
			}
			for(DeclVar v : varDecls){
				allFreeVars.remove(v.getName());
			}
			c.addAll(allFreeVars);
			return ImmutableList.copyOf(newDecls);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
