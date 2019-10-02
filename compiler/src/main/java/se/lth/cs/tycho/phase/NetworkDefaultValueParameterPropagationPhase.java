package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.NlNetworks;
import se.lth.cs.tycho.attribute.ParameterDeclarations;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.Collections;
import java.util.Set;

public class NetworkDefaultValueParameterPropagationPhase implements Phase {
    @Override
    public String getDescription() {
        return "Top network default value parameter propagation.";
    }

    @Override
    public Set<Class<? extends Phase>> dependencies() {
        return Collections.singleton(NetworkParameterAnalysisPhase.class);
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
        if (entityDecl.getEntity() instanceof NlNetwork) {
            NlNetwork nlNetwork = (NlNetwork) entityDecl.getEntity();

            Transformation t = MultiJ.from(Transformation.class)
                    .bind("parent").to(nlNetwork)
                    .bind("nlNetworks").to(task.getModule(NlNetworks.key))
                    .bind("parameterDecls").to(task.getModule(ParameterDeclarations.key))
                    .bind("varDecls").to(task.getModule(VariableDeclarations.key))
                    .instance();

            return task.transformChildren(t);

        }
        return task;
    }


    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        NlNetwork parent();

        @Binding(BindingKind.INJECTED)
        NlNetworks nlNetworks();

        @Binding(BindingKind.INJECTED)
        ParameterDeclarations parameterDecls();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations varDecls();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default ValueParameter apply(ValueParameter valueParameter) {
            if (nlNetworks().enclosingNetwork(valueParameter) == parent()) {
                // -- ParameterVarDecl varDecl = parameterDecls().valueParameterDeclaration(valueParameter);
                Expression expr = visit(valueParameter.getValue());
                return new ValueParameter(valueParameter.getName(), expr.deepClone());
            } else {
                return valueParameter;
            }
        }

        default Expression visit(Expression expr) {
            return expr;
        }

        default Expression visit(ExprBinaryOp exprOp) {
            Expression op0 = visit(exprOp.getOperands().get(0));
            Expression op1 = visit(exprOp.getOperands().get(1));
            ImmutableList.Builder<Expression> operands = ImmutableList.builder();
            operands.add(op0);
            operands.add(op1);
            return new ExprBinaryOp(exprOp.getOperations(), operands.build());
        }


        default Expression visit(ExprVariable exprVariable) {
            VarDecl varDecl = varDecls().declaration(exprVariable);

            if (varDecl != null) {
                if (varDecl instanceof ParameterVarDecl) {
                    ParameterVarDecl paramDecl = (ParameterVarDecl) varDecl;
                    if (paramDecl.getDefaultValue() != null) {
                        return paramDecl.getDefaultValue().deepClone();
                    }
                }
            }

            return exprVariable;
        }


    }

}
