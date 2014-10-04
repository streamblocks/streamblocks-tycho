package net.opendf.analyze.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Variable;
import net.opendf.ir.common.decl.LocalVarDecl;
import net.opendf.ir.common.decl.ParDeclValue;
import net.opendf.ir.common.decl.VarDecl;
import net.opendf.ir.common.expr.ExprLambda;
import net.opendf.ir.common.expr.ExprLet;
import net.opendf.ir.common.expr.ExprProc;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.common.stmt.Statement;
import net.opendf.ir.common.stmt.StmtBlock;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ActorTransformerWrapper;
import net.opendf.transform.util.ErrorAwareBasicTransformer;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;

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

public class VariableInitOrderTransformer extends ErrorAwareBasicTransformer<Set<String>> {

	
	public VariableInitOrderTransformer(SourceCodeOracle sourceOracle) {
		super(sourceOracle);
	}

	//--- wrappers ------------------------------------------------------------
	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static Actor transformActor(Actor actor, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer(sourceOracle);
		ActorTransformerWrapper<Set<String>> wrapper = new ActorTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		actor = wrapper.transformActor(actor, c);
		freeVarTransformer.printWarnings();
		freeVarTransformer.abortIfError();
		return actor;
	}

	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static ActorMachine transformActorMachine(ActorMachine actorMachine, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer(sourceOracle);
		ActorMachineTransformerWrapper<Set<String>> wrapper = new ActorMachineTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		actorMachine = wrapper.transformActorMachine(actorMachine, c);
		freeVarTransformer.printWarnings();
		freeVarTransformer.abortIfError();
		return actorMachine;
	}

	/**
	 * Order all variable initializations to a valid initialization order, i.e. a variable is only depending on variables earlier in the list.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		VariableInitOrderTransformer freeVarTransformer = new VariableInitOrderTransformer(sourceOracle);
		NetworkDefinitionTransformerWrapper<Set<String>> wrapper = new NetworkDefinitionTransformerWrapper<Set<String>>(freeVarTransformer);
		Set<String> c = new TreeSet<String>();  //sort the free variables in alphabetic order
		net = wrapper.transformNetworkDefinition(net, c);
		freeVarTransformer.printWarnings();
		freeVarTransformer.abortIfError();
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
			for(ParDeclValue v :lambda.getValueParameters()){
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
			ImmutableList<LocalVarDecl> newDecls = transformVarDecls(let.getVarDecls(), freeVars);

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
			ImmutableList<LocalVarDecl> newDecls = transformVarDecls(block.getVarDecls(), freeVars);

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
	public ImmutableList<LocalVarDecl> transformVarDecls(ImmutableList<LocalVarDecl> varDecls, Set<String> c){
		assert varDecls != null;
		Set<String> allFreeVars;
		try {
			ImmutableList.Builder<LocalVarDecl> builder = new ImmutableList.Builder<>();
			allFreeVars = c.getClass().newInstance();
			int size = varDecls.size();
			HashMap<String, Set<String>> freeVarsMap = new HashMap<String, Set<String>>();
			LocalVarDecl[] newDecls = new LocalVarDecl[size];
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
			
			for(LocalVarDecl v : varDecls){
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

	public void scheduleDecls(int candidateIndex, LocalVarDecl[] decls, ScheduleStatus[] status, Map<String, Set<String>> freeVarsMap, ImmutableList.Builder<LocalVarDecl> builder) {
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
			error("Cyclic dependency when initializing variables. Dependent variables: " + sb, decls[candidateIndex]);
			return;
		case Scheduled:
			return;
		case nop :
			LocalVarDecl candidate = decls[candidateIndex];
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
