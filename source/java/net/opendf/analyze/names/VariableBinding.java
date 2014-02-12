package net.opendf.analyze.names;

import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Action;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.cal.InputPattern;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.Variable;

public class VariableBinding extends Module<VariableBinding.Required> {

	@Synthesized
	public IRNode declaration(Variable variable) {
		if (variable.isScopeVariable()) {
			return get().lookupScopeVariable(variable, variable.getScopeId(), variable.getName());
		} else {
			return get().lookupVariable(variable, variable.getName());
		}
	}

	@Inherited
	public IRNode lookupScopeVariable(ActorMachine actorMachine, int id, String name) {
		return lookupInList(actorMachine.getScope(id), name);
	}

	@Inherited
	public IRNode lookupVariable(ExprLet let, String name) {
		IRNode decl = lookupInList(let.getVarDecls(), name);
		return decl != null ? decl : get().lookupVariable(let, name);
	}
	
	@Inherited
	public IRNode lookupVariable(ActorMachine actorMachine, String name) {
		return null;
	}

	@Inherited
	public IRNode lookupVariable(StmtBlock block, String name) {
		IRNode decl = lookupInList(block.getVarDecls(), name);
		return decl != null ? decl : get().lookupVariable(block, name);
	}
	
	@Inherited
	public IRNode lookupVariable(StmtForeach foreach, String name) {
		for (GeneratorFilter generator : foreach.getGenerators()) {
			for (DeclVar decl : generator.getVariables()) {
				if (decl.getName().equals(name)) {
					return decl;
				}
			}
		}
		return get().lookupVariable(foreach, name);
	}

	@Inherited
	public IRNode lookupVariable(ExprList list, String name) {
		for (GeneratorFilter generator : list.getGenerators()) {
			for (DeclVar decl : generator.getVariables()) {
				if (decl.getName().equals(name)) {
					return decl;
				}
			}
		}
		return get().lookupVariable(list, name);
	}

	@Inherited
	public IRNode lookupVariable(ExprLambda lambda, String name) {
		IRNode decl = lookupInList(lambda.getValueParameters(), name);
		return decl != null ? decl : get().lookupVariable(lambda, name);
	}

	@Inherited
	public IRNode lookupVariable(ExprProc proc, String name) {
		IRNode decl = lookupInList(proc.getValueParameters(), name);
		return decl != null ? decl : get().lookupVariable(proc, name);
	}

	@Inherited
	public IRNode lookupVariable(Action action, String name) {
		IRNode decl = lookupInList(action.getVarDecls(), name);
		if (decl != null) {
			return decl;
		}
		for (InputPattern input : action.getInputPatterns()) {
			IRNode d = lookupInList(input.getVariables(), name);
			if (d != null) {
				return d;
			}
		}
		return get().lookupVariable(action, name);
	}
	
	@Inherited
	public IRNode lookupVariable(Actor actor, String name) {
		IRNode varDecl = lookupInList(actor.getVarDecls(), name);
		if (varDecl != null) {
			return varDecl;
		}
		IRNode parDecl = lookupInList(actor.getValueParameters(), name);
		if (parDecl != null) {
			return parDecl;
		}
		return null;
	}

	private IRNode lookupInList(Iterable<? extends IRNode> decls, String name) {
		for (IRNode node : decls) {
			String declName = null;
			if (node instanceof DeclVar) {
				declName = ((DeclVar) node).getName();
			} else if (node instanceof ParDeclValue) {
				declName = ((ParDeclValue) node).getName();
			}
			if (declName != null && declName.equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	public interface Required {
		public IRNode lookupVariable(IRNode node, String name);
		public IRNode lookupScopeVariable(IRNode node, int id, String name);
	}

}
