package net.opendf.analyze.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import net.opendf.interp.exception.CALCompiletimeException;
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
	
	@Override
	public ImmutableList<DeclVar> transformVarDecls(ImmutableList<DeclVar> varDecls, Set<String> c){
		assert varDecls != null;
		Set<String> allFreeVars;
		try {
			ImmutableList.Builder<DeclVar> builder = new ImmutableList.Builder<>();
			allFreeVars = c.getClass().newInstance();
			int size = varDecls.size();
			HashMap<String, Set<String>> freeVarsMap = new HashMap<String, Set<String>>();
			DeclVar[] newDecls = new DeclVar[size];
			ScheduleStatus[] status = new ScheduleStatus[size];
			// compute the free variables for each declaration
			for(int i=0; i<size; i++){
				Set<String> freeVars = c.getClass().newInstance();
				newDecls[i] = transformVarDecl(varDecls.get(i), freeVars);
				status[i] = ScheduleStatus.nop;
				freeVarsMap.put(varDecls.get(i).getName(), freeVars);				
				allFreeVars.addAll(freeVars);
			}
			
			for(int i=0; i<newDecls.length; i++){
				scheduleDecls(i, newDecls, status, freeVarsMap, builder);
			}
			
			for(DeclVar v : varDecls){
				allFreeVars.remove(v.getName());
			}
			c.addAll(allFreeVars);
			return builder.build();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private enum ScheduleStatus{nop, Visited, Scheduled}

	public void scheduleDecls(int candidateIndex, DeclVar[] decls, ScheduleStatus[] status, Map<String, Set<String>> freeVarsMap, ImmutableList.Builder<DeclVar> builder) {
		switch(status[candidateIndex]){
		case Visited:
			// A variable is depending on itself. Find the variables involved in the dependency cycle
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for(int i=0; i<status.length; i++){
				if(status[i]==ScheduleStatus.Visited){
					sb.append(sep);
					sb.append(decls[i].getName());
					sep = ", ";
				}
			}
			throw new CALCompiletimeException("Cyclic dependency when initializing variables. Dependent variables: " + sb);
		case Scheduled:
			return;
		case nop :
			DeclVar candidate = decls[candidateIndex];
			status[candidateIndex] = ScheduleStatus.Visited;
			for(String freeVar : freeVarsMap.get(candidate.getName())){
				for(int i=0; i<decls.length; i++){
					DeclVar decl = decls[i];
					if(status[i] != ScheduleStatus.Scheduled && decl.getName().equals(freeVar)){
						// the candidate is depending on an uninitialized variable. Initialize it first
						scheduleDecls(i, decls, status, freeVarsMap, builder);
					}
				}
			}
			status[candidateIndex] = ScheduleStatus.Scheduled;
			builder.add(candidate);
			break;
		}
	}

}
