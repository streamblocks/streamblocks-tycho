package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterTypeDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.phase.TreeShadow;

public interface ParameterDeclarations {
    ModuleKey<ParameterDeclarations> key = task -> MultiJ.from(Implementation.class)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .bind("entities").to(task.getModule(EntityDeclarations.key))
            .instance();

    ParameterVarDecl valueParameterDeclaration(ValueParameter parameter);
    ParameterTypeDecl typeParameterDeclaration(TypeParameter parameter);

    @Module
    interface Implementation extends ParameterDeclarations {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        EntityDeclarations entities();

        @Override
        default ParameterVarDecl valueParameterDeclaration(ValueParameter parameter) {
            return valueParameterLookup(tree().parent(parameter), parameter.getName());
        }

        default ParameterVarDecl valueParameterLookup(IRNode node, String name) {
            return null;
        }

        default ParameterVarDecl valueParameterLookup(EntityInstanceExpr entityInstance, String name) {
            GlobalEntityDecl entity = entities().declaration(entityInstance.getEntityName());
            if (entity == null) return null;
            return entity.getEntity().getValueParameters().stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }

        default ParameterVarDecl valueParameterLookup(TypeExpr type, String name) {
            return null;
        }

        @Override
        default ParameterTypeDecl typeParameterDeclaration(TypeParameter parameter) {
            return typeParameterLookup(tree().parent(parameter), parameter.getName());
        }

        default ParameterTypeDecl typeParameterLookup(IRNode node, String name) {
            return null;
        }
    }
}
