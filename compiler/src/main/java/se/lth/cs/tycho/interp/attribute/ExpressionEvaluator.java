package se.lth.cs.tycho.interp.attribute;


import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.attribute.FreeVariables;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.interp.Environment;
import se.lth.cs.tycho.interp.Interpreter;
import se.lth.cs.tycho.interp.Stack;
import se.lth.cs.tycho.interp.TypeConverter;
import se.lth.cs.tycho.interp.values.ConstRef;
import se.lth.cs.tycho.interp.values.Function;
import se.lth.cs.tycho.interp.values.LambdaFunction;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;

@Module
public interface ExpressionEvaluator {

    @Binding(BindingKind.INJECTED)
    Interpreter interpreter();

    @Binding(BindingKind.INJECTED)
    Stack stack();

    @Binding(BindingKind.INJECTED)
    TypeConverter converter();

    @Binding(BindingKind.INJECTED)
    Types types();

    @Binding(BindingKind.INJECTED)
    FreeVariables freeVariables();

    @Binding(BindingKind.INJECTED)
    VariableDeclarations declarations();

    RefView evaluate(Expression expression, Environment environment);

    default RefView evaluate(ExprApplication expression, Environment environment) {
        RefView r = evaluate(expression.getFunction(), environment);
        Function f = converter().getFunction(r);
        ImmutableList<Expression> args = expression.getArgs();
        for (Expression arg : args) {
            stack().push(evaluate(arg, environment));
        }
        return f.apply(interpreter());
    }

    default RefView evaluate(ExprBinaryOp expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprBinaryOp is not supported"));
    }

    default RefView evaluate(ExprComprehension expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprComprehension is not supported"));
    }

    default RefView evaluate(ExprDeref expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprDeref is not supported"));
    }

    default RefView evaluate(ExprField expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprField is not supported"));
    }

    default RefView evaluate(ExprGlobalVariable expression, Environment environment) {
        VarDecl decl = declarations().declaration(expression);

        if (decl.isExternal()) {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "External ExprGlobalVariable is not supported."));
        }

        if (decl.isConstant()) {
            return evaluate(decl.getValue(), environment);
        }else{
            return null;
        }
    }

    default RefView evaluate(ExprIf expression, Environment environment) {
        RefView c = evaluate(expression.getCondition(), environment);
        Expression e = converter().getBoolean(c) ? expression.getThenExpr() : expression.getElseExpr();
        return evaluate(e, environment);
    }

    default RefView evaluate(ExprIndexer expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprIndexer is not supported"));
    }

    default RefView evaluate(ExprInput expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprInput is not supported"));
    }

    default RefView evaluate(ExprLambda expression, Environment environment) {
        Environment closureEnv = environment.closure(freeVariables().freeVariables(expression), stack());
        Function f = new LambdaFunction(expression, closureEnv);
        converter().setFunction(stack().push(), f);
        return stack().pop();
    }

    default RefView evaluate(ExprLet expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprLet is not supported"));
    }

    default RefView evaluate(ExprList expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprList is not supported"));
    }

    default RefView evaluate(ExprLiteral expression, Environment environment) {
        RefView value = null;

        ExprLiteral.Kind kind = expression.getKind();
        if (kind == ExprLiteral.Kind.False) {
            value = ConstRef.of(false);
        } else if (kind == ExprLiteral.Kind.True) {
            value = ConstRef.of(true);
        } else if (kind == ExprLiteral.Kind.Integer) {
            value = ConstRef.of(expression.asInt().getAsInt());
        } else if (kind == ExprLiteral.Kind.Real) {
            value = ConstRef.of(expression.asDouble().getAsDouble());
        } else if (kind == ExprLiteral.Kind.String) {
            value = ConstRef.of(expression.asString().get());
        } else {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "This ExprLiteral is not supported."));
        }

        return value;
    }

    default RefView evaluate(ExprMap expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprMap is not supported"));
    }

    default RefView evaluate(ExprProc expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprProc is not supported"));
    }

    default RefView evaluate(ExprRef expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprRef is not supported"));
    }

    default RefView evaluate(ExprSet expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprSet is not supported"));
    }

    default RefView evaluate(ExprUnaryOp expression, Environment environment) {
        throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprUnaryOp is not supported"));
    }

    default RefView evaluate(ExprVariable expression, Environment environment) {
        VarDecl decl = declarations().declaration(expression);
        if (decl.isExternal()) {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "External ExprVariable is not supported."));
        }

        if (decl.isConstant()) {
            return evaluate(decl.getValue(), environment);
        }else{
            return null;
        }


    }


}
