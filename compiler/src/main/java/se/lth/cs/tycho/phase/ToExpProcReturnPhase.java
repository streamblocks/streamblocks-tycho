package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.ExprProcReturn;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtReturn;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

public class ToExpProcReturnPhase implements Phase {
    @Override
    public String getDescription() {
        return "Transform ExprProc and ExprLambda/ExprLet to ExprProcReturn.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(Transformation.class)
                .instance();

        return task.transformChildren(transformation);
    }

    @Module
    interface Transformation extends IRNode.Transformation {
        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default IRNode apply(ExprProcReturn proc){

            ImmutableList<Statement> stmts = ImmutableList.from(proc.getBody());

            //If ExprProcReturn contains other stmts than a single StmtBlock
            if(stmts.size() > 1 || !(stmts.get(0) instanceof StmtBlock)){
                StmtBlock stmtBlock = new StmtBlock(ImmutableList.empty(), ImmutableList.empty(), stmts);
                return new ExprProcReturn(proc.getValueParameters(), ImmutableList.of(stmtBlock), proc.getReturnType());
            }
            return proc;
        }

        default IRNode apply(ExprProc proc) {
            ImmutableList.Builder<ParameterVarDecl> builderParameters = ImmutableList.builder();
            builderParameters.addAll(proc.getValueParameters());

            ImmutableList.Builder<Statement> builderBody = ImmutableList.builder();
            builderBody.addAll(proc.getBody());

            ExprProcReturn procReturn = new ExprProcReturn(builderParameters.build(), builderBody.build(), null);
            return procReturn;
        }

        default IRNode apply(ExprLambda lambda) {
            ImmutableList.Builder<ParameterVarDecl> builderParameters = ImmutableList.builder();
            builderParameters.addAll(lambda.getValueParameters());

            TypeExpr typeExpr = lambda.getReturnType();

            if (lambda.getBody() instanceof ExprLet) {
                ExprLet let = (ExprLet) lambda.getBody();

                ImmutableList.Builder<TypeDecl> typeDecls = ImmutableList.builder();
                ImmutableList.Builder<LocalVarDecl> varDecls = ImmutableList.builder();
                typeDecls.addAll(let.getTypeDecls());
                varDecls.addAll(let.getVarDecls());

                Expression expression = let.getBody().deepClone();
                StmtReturn ret = new StmtReturn(expression);

                StmtBlock block = new StmtBlock(typeDecls.build(), varDecls.build(), ImmutableList.of(ret));
                ExprProcReturn proc = new ExprProcReturn(builderParameters.build(), ImmutableList.of(block), typeExpr);
                return proc;
            } else {
                Expression expression = lambda.getBody().deepClone();
                StmtReturn ret = new StmtReturn(expression);

                ExprProcReturn proc = new ExprProcReturn(builderParameters.build(), ImmutableList.of(ret), typeExpr);
                return proc;
            }
        }
    }
}
