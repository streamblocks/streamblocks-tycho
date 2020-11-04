package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueDeref;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.type.FunctionTypeExpr;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.ProcedureTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.Type;

public class OrccListParameters implements Phase {
    @Override
    public String getDescription() {
        return "Pass lists by reference in orcc-files";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(Transformation.class)
                .bind("tree").to(task.getModule(TreeShadow.key))
                .bind("types").to(task.getModule(Types.key))
                .bind("variables").to(task.getModule(VariableDeclarations.key))
                .instance();
        return transformation.apply(task);
    }

    @Module
    interface Transformation extends IRNode.Transformation {
        @Binding(BindingKind.INJECTED)
        TreeShadow tree();

        @Binding(BindingKind.INJECTED)
        Types types();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations variables();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        default CompilationTask apply(CompilationTask task) {
            return task.transformChildren(this);
        }

        default Expression apply(Expression expr) {
            return expr.transformChildren(this);
        }


        default SourceUnit apply(SourceUnit unit) {
            if (unit.getLanguage() == SourceUnit.InputLanguage.ORCC) {
                return unit.transformChildren(this);
            } else {
                return unit;
            }
        }

        default ExprApplication apply(ExprApplication application) {
            return application.copy(apply(application.getFunction()), application.getArgs().map(this::transformArg));
        }

        default StmtCall apply(StmtCall call) {
            return call.copy(apply(call.getProcedure()), call.getArgs().map(this::transformArg));
        }

        default Expression transformArg(Expression expr) {
            if (isListType(types().type(expr))) {
                return ref(apply(expr));
            } else {
                return apply(expr);
            }
        }

        default Expression ref(Expression expr) {
            throw new CompilationException(
                    new Diagnostic(
                            Diagnostic.Kind.ERROR,
                            "Can only pass lists as variables",
                            sourceUnit(expr),
                            expr));
        }

        default Expression ref(ExprList exprList){
            return exprList;
        }

        default Expression ref(ExprDeref expr){
             return ref(expr.getReference());
        }

        default Expression ref(ExprVariable var) {
            return new ExprRef(var.getVariable());
        }

        default ParameterVarDecl apply(ParameterVarDecl parameter) {
            if (isListParameterDeclaration(parameter)) {
                TypeExpr type = parameter.getType();
                return parameter.transformChildren(this).withType(
                        refType(type));
            } else {
                return parameter.transformChildren(this);
            }
        }

        default NominalTypeExpr refType(TypeExpr type) {
            return new NominalTypeExpr(
                    "Ref",
                    ImmutableList.of(new TypeParameter("type", type)),
                    ImmutableList.of());
        }

        default Expression apply(ExprVariable var) {
            if (isListParameter(var.getVariable())) {
                return new ExprDeref(var);
            } else {
                return var;
            }
        }

        default LValue apply(LValueVariable var) {
            if (isListParameter(var.getVariable())) {
                return new LValueDeref(var);
            } else {
                return var;
            }
        }

        default TypeExpr apply(TypeExpr type) {
            return type.transformChildren(this);
        }

        default ProcedureTypeExpr apply(ProcedureTypeExpr type) {
            return type.withParameterTypes(type.getParameterTypes().map(this::transformParType));
        }

        default FunctionTypeExpr apply(FunctionTypeExpr type) {
            return type.withParameterTypes(type.getParameterTypes().map(this::transformParType));
        }

        default TypeExpr transformParType(TypeExpr type) {
            return apply(type);
        }

        default TypeExpr transformParType(NominalTypeExpr type) {
            if (type.getName().equals("List")) {
                return refType(type);
            } else {
                return apply(type);
            }
        }

        default boolean isListParameter(Variable var) {
            return isListParameterDeclaration(variables().declaration(var));
        }

        default boolean isListParameterDeclaration(VarDecl var) {
            return false;
        }

        default boolean isListParameterDeclaration(ParameterVarDecl var) {
            return isListType(types().declaredType(var));
        }

        default boolean isListType(Type type) {
            return false;
        }

        default boolean isListType(ListType type) {
            return true;
        }

        default SourceUnit sourceUnit(IRNode node) {
            return sourceUnit(tree().parent(node));
        }

        default SourceUnit sourceUnit(SourceUnit unit) {
            return unit;
        }

    }


}
