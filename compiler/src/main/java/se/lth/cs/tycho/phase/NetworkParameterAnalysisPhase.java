package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class NetworkParameterAnalysisPhase implements Phase {
    @Override
    public String getDescription() {
        return "Analysis of network parameters.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
        if (entityDecl.getEntity() instanceof NlNetwork) {
            NlNetwork nlNetwork = (NlNetwork) entityDecl.getEntity();
            CheckNetworkParameters visitor = MultiJ.from(CheckNetworkParameters.class)
                    .bind("tree").to(task.getModule(TreeShadow.key))
                    .bind("parent").to(nlNetwork)
                    .bind("entities").to(task.getModule(EntityDeclarations.key))
                    .bind("reporter").to(context.getReporter())
                    .instance();

            visitor.check(nlNetwork);
        }
        return task;
    }


    @Module
    interface CheckNetworkParameters {

        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        NlNetwork parent();

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Binding(BindingKind.INJECTED)
        Reporter reporter();

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }

        default void check(IRNode node) {
            checkParameters(node);
            node.forEachChild(this::check);
        }

        default void checkParameters(IRNode node) {
        }



        default void checkParameters(NlNetwork nlNetwork) {
            if (nlNetwork == parent()) {
                // -- Check top network Parameters if they have default values
                for (ParameterVarDecl decl : nlNetwork.getValueParameters()) {
                    if (decl.getDefaultValue() == null) {
                        reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Parameter declaration : " + decl.getName() + ", does not have a default value.", sourceUnit(decl), decl));
                    }
                }
            } else {
                nlNetwork.forEachChild(this::check);
            }
        }

        default void checkParameters(EntityInstanceExpr entityInstanceExpr) {
            EntityReferenceLocal ref = ((EntityReferenceLocal) entityInstanceExpr.getEntityName());
            GlobalEntityDecl globalEntityDecl = entities().declaration(ref);
            int parameterAssignments = entityInstanceExpr.getParameterAssignments().size();
            int entityParameterDeclarations = globalEntityDecl.getEntity().getValueParameters().size();
            // -- Check the number of value parameters given to entity
            if (parameterAssignments != entityParameterDeclarations) {
                reporter().report(new Diagnostic(Diagnostic.Kind.ERROR,
                        "Wrong number of value parameters in instance expression, expected : " + entityParameterDeclarations + ", given : " + parameterAssignments + ".",
                        sourceUnit(entityInstanceExpr),
                        entityInstanceExpr));
            }
            // -- Check sub network
            if (globalEntityDecl.getEntity() instanceof NlNetwork) {
                check(globalEntityDecl.getEntity());
            }
        }

    }

}
