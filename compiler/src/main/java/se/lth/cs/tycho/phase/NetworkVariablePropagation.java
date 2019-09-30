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
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Instance;
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
            List<LocalVarDecl> varDecls = new ArrayList<>();
            varDecls.addAll(nlNetwork.getVarDecls());

            // -- FIXME : recursive
            for(InstanceDecl entity : nlNetwork.getEntities()){
                assert entity.getEntityExpr() instanceof EntityInstanceExpr;
                EntityInstanceExpr expr = (EntityInstanceExpr) entity.getEntityExpr();
                assert expr.getEntityName() instanceof EntityReferenceGlobal;
                EntityReferenceLocal ref = ((EntityReferenceLocal) expr.getEntityName());
                GlobalEntityDecl e = entities.declaration(ref);
                if(e.getEntity() instanceof NlNetwork){
                    varDecls.addAll(nlNetwork.getVarDecls());
                }
            }




            Transformation t = MultiJ.from(Transformation.class)
                    .bind("varDecls").to(varDecls)
                    .instance();

            return task.transformChildren(t);
        }

        return task;
    }

    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        List varDecls();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default NlNetwork apply(NlNetwork nlNetwork) {
                List<VarDecl> decls = varDecls();
            for(VarDecl decl : nlNetwork.getVarDecls()){
                VarDecl d = decls.stream().filter(v -> v.getName().equals(decl.getName())).findAny().orElse(null);
                if(d == null){
                    varDecls().add(decl);
                }
            }

            return nlNetwork.transformChildren(this);
        }

        default ValueParameter apply(ValueParameter valueParameter) {

            List<VarDecl> vars = varDecls();

            if (!vars.isEmpty()) {
                if (valueParameter.getValue() instanceof ExprVariable) {
                    ExprVariable exprVariable = (ExprVariable) valueParameter.getValue();
                    Expression expr = vars.stream()
                            .filter(v -> v.getName().equals(exprVariable.getVariable().getName()))
                            .map(VarDecl::getValue)
                            .findFirst()
                            .orElse(null);
                    if (expr != null) {
                        return new ValueParameter(valueParameter.getName(), expr.deepClone());
                    }
                }
            }

            return valueParameter;
        }
    }

}
