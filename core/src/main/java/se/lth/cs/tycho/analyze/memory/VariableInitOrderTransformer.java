package se.lth.cs.tycho.analyze.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.AbstractBasicTransformer;
import se.lth.cs.tycho.transform.util.ActorMachineTransformerWrapper;
import se.lth.cs.tycho.transform.util.ActorTransformerWrapper;
import se.lth.cs.tycho.transform.util.NetworkDefinitionTransformerWrapper;

/**
 * This Transformation computes the set of free variables for any expression.
 * The set of free variables are cached in the ExprLambda and ExprProc classes.
 * 
 * It also orders variable declarations to a valid initialization order.
 * 
 * Semantic Checks:
 * - Cyclic dependencies in variable initialization is detected.
 * 
 * Prerequisites:
 * - non
 * 
 * @author pera
 */

public class VariableInitOrderTransformer extends AbstractBasicTransformer<Set<String>> {


	//--- wrappers ------------------------------------------------------------
	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static CalActor transformActor(CalActor calActor) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer();
		ActorTransformerWrapper<Set<String>> wrapper = new ActorTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		calActor = wrapper.transformActor(calActor, c);
		return calActor;
	}

	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static ActorMachine transformActorMachine(ActorMachine actorMachine) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer();
		ActorMachineTransformerWrapper<Set<String>> wrapper = new ActorMachineTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		actorMachine = wrapper.transformActorMachine(actorMachine, c);
		return actorMachine;
	}

	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static NlNetwork transformNetworkDefinition(NlNetwork net) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer();
		NetworkDefinitionTransformerWrapper<Set<String>> wrapper = new NetworkDefinitionTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		net = wrapper.transformNetworkDefinition(net, c);
		return net;
	}

	//--- transformations ---------------------------------------------------------------

	/**************************************************************************************************************
	 * computing the free variables
	 */

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
			for(VarDecl v :lambda.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.variable(name));
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
			ImmutableList<VarDecl> newDecls = transformVarDecls(let.getVarDecls(), freeVars);

			Expression body = let.getBody().accept(this, freeVars);
			// remove the locally declared names
			for(VarDecl v :let.getVarDecls()){
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
			ImmutableList<VarDecl> newDecls = transformVarDecls(block.getVarDecls(), freeVars);

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
			for(VarDecl v : proc.getValueParameters()){
				freeVars.remove(v.getName());
			}
			ImmutableList.Builder<Variable> builder = new ImmutableList.Builder<Variable>();
			for(String name : freeVars){
				builder.add(Variable.variable(name));
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
	public ImmutableList<VarDecl> transformVarDecls(ImmutableList<VarDecl> varDecls, Set<String> c){
		assert varDecls != null;
		Set<String> allFreeVars;
		try {
			ImmutableList.Builder<VarDecl> builder = new ImmutableList.Builder<>();
			allFreeVars = c.getClass().newInstance();
			int size = varDecls.size();
			HashMap<String, Set<String>> freeVarsMap = new HashMap<String, Set<String>>();
			VarDecl[] newDecls = new VarDecl[size];
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
			
			for(VarDecl v : varDecls){
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

	public void scheduleDecls(int candidateIndex, VarDecl[] decls, ScheduleStatus[] status, Map<String, Set<String>> freeVarsMap, ImmutableList.Builder<VarDecl> builder) {
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
			throw new RuntimeException("Cyclic dependency when initializing variables. Dependent variables: " + sb);
		case Scheduled:
			return;
		case nop :
			VarDecl candidate = decls[candidateIndex];
			status[candidateIndex] = ScheduleStatus.Visited;
			for(String freeVar : freeVarsMap.get(candidate.getName())){
				for(int i=0; i<decls.length; i++){
					VarDecl decl = decls[i];
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
