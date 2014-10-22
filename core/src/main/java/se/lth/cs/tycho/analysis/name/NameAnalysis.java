package se.lth.cs.tycho.analysis.name;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javarag.Collected;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import javarag.coll.Builder;
import javarag.coll.Builders;
import javarag.coll.Collector;
import se.lth.cs.tycho.analysis.util.TreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRootModule;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.Scope;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.Import;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.util.Result;

public class NameAnalysis extends Module<NameAnalysis.Attributes> {

	public interface Attributes extends Declarations, Imports.Decls, TreeRootModule.Declarations, NamespaceDecls.Declarations {}
	
	public interface Declarations {
		@Synthesized
		public Result<VarDecl> variableDeclaration(Variable variable);

		@Inherited
		public Result<VarDecl> lookupVariable(IRNode node, Variable variable);
		
		@Inherited
		public Result<VarDecl> lookupScopeVariable(IRNode node, Variable variable);
		
		@Collected
		public Set<Message> nameErrors(TreeRoot root);
		
		@Synthesized
		public Result<VarDecl> lookupVariableInLocation(NamespaceDecl ns, Variable var);
		
	}
	
	public Builder<Set<Message>, Message> nameErrors(TreeRoot root) {
		return Builders.setBuilder();
	}
	
	public void nameErrors(TreeRoot root, Collector<Message> coll) {
		IRNode mainTree = e().mainTree(root);
		coll.collectFrom(mainTree);
	}
	
	public void nameErrors(Variable var, Collector<Message> coll) {
		Result<VarDecl> decl = e().variableDeclaration(var);
		if (decl.isFailure()) {
			coll.add(e().treeRoot(var), decl.getMessage());
		}
	}

	public Result<VarDecl> variableDeclaration(Variable variable) {
		if (variable.isScopeVariable()) {
			return e().lookupScopeVariable(variable, variable);
		} else {
			return e().lookupVariable(variable, variable);
		}
	}
	
	public Result<VarDecl> lookupScopeVariable(ActorMachine actorMachine, Variable variable) {
		int scope = variable.getScopeId();
		if (scope < actorMachine.getScopes().size() && scope >= 0) {	
			VarDecl decl = findInList(actorMachine.getScope(scope), variable.getName());
			if (decl != null) {
				return Result.success(decl);
			}
		}
		return Result.failure(Message.error("Undefined variable " + variable));
	}

	private <D extends Decl> D findInList(List<D> decls, String name) {
		for (D decl : decls) {
			if (decl.getName().equals(name)) {
				return decl;
			}
		}
		return null;
	}
	
	public Result<VarDecl> lookupVariable(TreeRoot root, Variable var) {
		String name = var.getName();
		if (name.startsWith("$UnaryOperation.") || name.startsWith("$BinaryOperation.")) {
			return Result.failure(Message.error("Could not find operator " + name + "."));
		}
		
 		return Result.failure(Message.error("Declaration for variable " + var.getName() + " not found"));
	}
	
	private <T extends IRNode> Result<VarDecl> lookupVariableHelper(T scope, Function<T, NamespaceDecl> getLocation, Variable var) {
		NamespaceDecl unit = getLocation.apply(scope);
		TreeRoot root = e().treeRoot(scope);
		DeclarationLoader loader = root.getLoader();
		while (unit != null && loader.getLocation(unit) != null) {
			unit = loader.getLocation(unit);
		}
		if (unit != null) {
			e().compilationUnit(root, unit);
			return e().lookupVariableInLocation(getLocation.apply(scope), var);
		} else {
			return Result.failure(Message.error("Declaration for variable " + var.getName() + " not found"));
		}

	}
	
	public Result<VarDecl> lookupVariable(Scope scope, Variable var) {
		return lookupVariableHelper(scope, Scope::getLocation, var);
	}
	
	public Result<VarDecl> lookupVariable(Transition transition, Variable var) {
		return lookupVariableHelper(transition, Transition::getLocation, var);
	}

	public Result<VarDecl> lookupVariable(PredicateCondition cond, Variable var) {
		return lookupVariableHelper(cond, PredicateCondition::getLocation, var);

	}

	public Result<VarDecl> lookupVariableInLocation(NamespaceDecl ns, Variable var) {
		for (Import imp : ns.getImports()) {
			Result<Optional<VarDecl>> imported = e().importVar(imp, var.getName());
			if (imported.isFailure()) {
				return Result.failure(imported.getMessage());
			} else if (imported.get().isPresent()) {
				return Result.success(imported.get().get());
			}
		}
		VarDecl decl = findInList(ns.getVarDecls(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		}
		return e().lookupVariable(ns, var);
	}
	
	public Result<VarDecl> lookupVariable(NamespaceDecl ns, Variable var) {
		return e().lookupVariableInLocation(ns, var);
	}

	public Result<VarDecl> lookupVariable(ExprLet let, Variable var) {
		VarDecl decl = findInList(let.getVarDecls(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		} else {
			return e().lookupVariable(let, var);
		}
	}

	public Result<VarDecl> lookupVariable(StmtBlock block, Variable var) {
		VarDecl decl = findInList(block.getVarDecls(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		} else {
			return e().lookupVariable(block, var);
		}
	}

	public Result<VarDecl> lookupVariable(StmtForeach foreach, Variable var) {
		for (GeneratorFilter generator : foreach.getGenerators()) {
			for (VarDecl decl : generator.getVariables()) {
				if (decl.getName().equals(var.getName())) {
					return Result.success(decl);
				}
			}
		}
		return e().lookupVariable(foreach, var);
	}

	public Result<VarDecl> lookupVariable(ExprList list, Variable var) {
		for (GeneratorFilter generator : list.getGenerators()) {
			for (VarDecl decl : generator.getVariables()) {
				if (decl.getName().equals(var.getName())) {
					return Result.success(decl);
				}
			}
		}
		return e().lookupVariable(list, var);
	}

	public Result<VarDecl> lookupVariable(ExprLambda lambda, Variable var) {
		VarDecl decl = findInList(lambda.getValueParameters(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		} else {
			return e().lookupVariable(lambda, var);
		}
	}

	public Result<VarDecl> lookupVariable(ExprProc proc, Variable var) {
		VarDecl decl = findInList(proc.getValueParameters(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		} else {
			return e().lookupVariable(proc, var);
		}
	}

	public Result<VarDecl> lookupVariable(Action action, Variable var) {
		VarDecl decl = findInList(action.getVarDecls(), var.getName());
		if (decl != null) {
			return Result.success(decl);
		}
		for (InputPattern input : action.getInputPatterns()) {
			VarDecl d = findInList(input.getVariables(), var.getName());
			if (d != null) {
				return Result.success(d);
			}
		}
		return e().lookupVariable(action, var);
	}

	public Result<VarDecl> lookupVariable(CalActor calActor, Variable var) {
		VarDecl varDecl = findInList(calActor.getVarDecls(), var.getName());
		if (varDecl != null) {
			return Result.success(varDecl);
		}
		VarDecl parDecl = findInList(calActor.getValueParameters(), var.getName());
		if (parDecl != null) {
			return Result.success(parDecl);
		}
		return e().lookupVariable(calActor, var);
	}
}
