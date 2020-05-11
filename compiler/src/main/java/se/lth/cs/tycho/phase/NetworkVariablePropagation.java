package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.ArrayList;
import java.util.List;

public class NetworkVariablePropagation implements Phase {
    @Override
    public String getDescription() {
        return "Propagate network variables.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
        EntityDeclarations entities = task.getModule(EntityDeclarations.key);

        if (entityDecl.getEntity() instanceof NlNetwork) {
            NlNetwork nlNetwork = (NlNetwork) entityDecl.getEntity();
            List<VarDecl> varDecls = new ArrayList<>();

            varDecls.addAll(getAllVarDecl(nlNetwork, entities));

            Transformation t = MultiJ.from(Transformation.class)
                    .bind("varDecls").to(varDecls)
                    .instance();

            return task.transformChildren(t);
        }

        return task;
    }

    private List<VarDecl> getAllVarDecl(NlNetwork nlNetwork, EntityDeclarations entities) {
        List<VarDecl> vars = new ArrayList<>();
        vars.addAll(nlNetwork.getVarDecls());
        for (InstanceDecl entity : nlNetwork.getEntities()) {
            assert entity.getEntityExpr() instanceof EntityInstanceExpr;
            EntityInstanceExpr expr = (EntityInstanceExpr) entity.getEntityExpr();
            assert expr.getEntityName() instanceof EntityReferenceGlobal;
            EntityReferenceLocal ref = ((EntityReferenceLocal) expr.getEntityName());
            GlobalEntityDecl e = entities.declaration(ref);
            if (e.getEntity() instanceof NlNetwork) {
                NlNetwork nl = (NlNetwork) e.getEntity();
                vars.addAll(getAllVarDecl(nl, entities));
            }
        }

        return vars;
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        List varDecls();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default ValueParameter apply(ValueParameter valueParameter) {

            List<VarDecl> vars = varDecls();

            if (!vars.isEmpty()) {
                Expression expr = visit(valueParameter.getValue());
                return new ValueParameter(valueParameter.getName(), expr.deepClone());
            }

            return valueParameter;
        }

        default Expression visit(Expression expr) {
            return expr;
        }

        default Expression visit(ExprBinaryOp exprOp) {
            if (!varDecls().isEmpty()) {
                Expression op0 = visit(exprOp.getOperands().get(0));
                Expression op1 = visit(exprOp.getOperands().get(1));
                ImmutableList.Builder<Expression> operands = ImmutableList.builder();
                operands.add(op0);
                operands.add(op1);
                return new ExprBinaryOp(exprOp.getOperations(), operands.build());
            }
            return exprOp;
        }

        default Expression visit(ExprVariable exprVariable) {
            List<VarDecl> params = varDecls();
            Expression expr = params.stream()
                    .filter(p -> p.getName().equals(exprVariable.getVariable().getName()))
                    .map(VarDecl::getValue)
                    .findFirst()
                    .orElse(null);
            if (expr != null) {
                return expr;
            }

            return exprVariable;
        }

        default ExprUnaryOp visit(ExprUnaryOp exprUnaryOp){
            if(!varDecls().isEmpty()){
                Expression expr = visit(exprUnaryOp.getOperand());
                return new ExprUnaryOp(exprUnaryOp.getOperation(), expr);
            }

            return exprUnaryOp;
        }

    }

}
