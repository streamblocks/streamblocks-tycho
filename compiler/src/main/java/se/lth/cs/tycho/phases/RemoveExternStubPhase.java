package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.FunctionTypeExpr;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ProcedureTypeExpr;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprProc;

public class RemoveExternStubPhase implements Phase {
	@Override
	public String getDescription() {
		return "Removes stubs of external function and procedure declarations.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return (CompilationTask) transformAll(task);
	}

	private ProcedureTypeExpr getProcedureType(ExprProc proc) {
		return new ProcedureTypeExpr(proc.getValueParameters().map(ParameterVarDecl::getType));
	}

	private FunctionTypeExpr getFunctionType(ExprLambda lambda) {
		return new FunctionTypeExpr(lambda.getValueParameters().map(ParameterVarDecl::getType), lambda.getReturnType());
	}

	private IRNode transformAll(IRNode node) {
		return transform(node.transformChildren(this::transformAll));
	}

	private IRNode transform(IRNode node) {
		if (node instanceof VarDecl) {
			VarDecl decl = (VarDecl) node;
			if (decl.isExternal()) {
				if (decl.getValue() instanceof ExprLambda) {
					return removeStub(decl, getFunctionType((ExprLambda) decl.getValue()));
				} else if (decl.getValue() instanceof ExprProc) {
					return removeStub(decl, getProcedureType((ExprProc) decl.getValue()));
				} else {
					return decl;
				}
			} else {
				return decl;
			}
		} else {
			return node;
		}
	}

	private VarDecl removeStub(VarDecl decl, TypeExpr type) {
		if (decl instanceof LocalVarDecl) {
			return ((LocalVarDecl) decl).withValue(null).withType(type);
		}
		if (decl instanceof GlobalVarDecl) {
			return ((GlobalVarDecl) decl).withValue(null).withType(type);
		}
		throw new AssertionError("Only LocalVarDecl and GlobalVarDecl can be external.");
	}
}
