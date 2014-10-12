package se.lth.cs.tycho.analyze.names;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;

public class VariableBinding extends Module<VariableBinding.Decls> {

	public interface Decls {
		@Synthesized
		public IRNode declaration(Variable variable);

		@Inherited
		public IRNode lookupVariable(IRNode node, String name);

		@Inherited
		public IRNode lookupScopeVariable(IRNode node, int id, String name);
	}

	public IRNode declaration(Variable variable) {
		if (variable.isScopeVariable()) {
			return e().lookupScopeVariable(variable, variable.getScopeId(), variable.getName());
		} else {
			return e().lookupVariable(variable, variable.getName());
		}
	}

	public IRNode lookupScopeVariable(ActorMachine actorMachine, int id, String name) {
		return lookupInList(actorMachine.getScope(id), name);
	}

	public IRNode lookupVariable(ExprLet let, String name) {
		IRNode decl = lookupInList(let.getVarDecls(), name);
		return decl != null ? decl : e().lookupVariable(let, name);
	}

	public IRNode lookupVariable(ActorMachine actorMachine, String name) {
		return null;
	}

	public IRNode lookupVariable(StmtBlock block, String name) {
		IRNode decl = lookupInList(block.getVarDecls(), name);
		return decl != null ? decl : e().lookupVariable(block, name);
	}

	public IRNode lookupVariable(StmtForeach foreach, String name) {
		for (GeneratorFilter generator : foreach.getGenerators()) {
			for (VarDecl decl : generator.getVariables()) {
				if (decl.getName().equals(name)) {
					return decl;
				}
			}
		}
		return e().lookupVariable(foreach, name);
	}

	public IRNode lookupVariable(ExprList list, String name) {
		for (GeneratorFilter generator : list.getGenerators()) {
			for (VarDecl decl : generator.getVariables()) {
				if (decl.getName().equals(name)) {
					return decl;
				}
			}
		}
		return e().lookupVariable(list, name);
	}

	public IRNode lookupVariable(ExprLambda lambda, String name) {
		IRNode decl = lookupInList(lambda.getValueParameters(), name);
		return decl != null ? decl : e().lookupVariable(lambda, name);
	}

	public IRNode lookupVariable(ExprProc proc, String name) {
		IRNode decl = lookupInList(proc.getValueParameters(), name);
		return decl != null ? decl : e().lookupVariable(proc, name);
	}

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
		return e().lookupVariable(action, name);
	}

	public IRNode lookupVariable(CalActor calActor, String name) {
		IRNode varDecl = lookupInList(calActor.getVarDecls(), name);
		if (varDecl != null) {
			return varDecl;
		}
		IRNode parDecl = lookupInList(calActor.getValueParameters(), name);
		if (parDecl != null) {
			return parDecl;
		}
		return null;
	}

	private IRNode lookupInList(Iterable<? extends IRNode> decls, String name) {
		for (IRNode node : decls) {
			String declName = null;
			if (node instanceof LocalVarDecl) {
				declName = ((VarDecl) node).getName();
			} else if (node instanceof ParDeclValue) {
				declName = ((ParDeclValue) node).getName();
			}
			if (declName != null && declName.equals(name)) {
				return node;
			}
		}
		return null;
	}

}
