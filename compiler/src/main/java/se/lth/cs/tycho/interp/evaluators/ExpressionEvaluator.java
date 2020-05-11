package se.lth.cs.tycho.interp.evaluators;


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
import se.lth.cs.tycho.interp.values.*;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.IntType;
import se.lth.cs.tycho.type.RealType;
import se.lth.cs.tycho.type.Type;

import java.util.OptionalDouble;
import java.util.OptionalLong;

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
        assert expression.getOperations().size() == 1 && expression.getOperands().size() == 2;
        String operation = expression.getOperations().get(0);
        Expression lExpression = expression.getOperands().get(0);
        Expression rExpression = expression.getOperands().get(1);

        Type lType = types().type(lExpression);
        Type rType = types().type(rExpression);

        RefView left = evaluate(lExpression, environment);
        RefView right = evaluate(rExpression, environment);

        if (operation.equals("..")) {
            int from = converter().getInt(left);
            int to = converter().getInt(right);
            BasicRef br = new BasicRef();
            br.setValue(new Range(from, to));
            return br;
        } else {
            if ((lType instanceof IntType) && (rType instanceof IntType)) {
                OptionalLong value = longValue(operation, converter().getInt(left), converter().getInt(right));
                return ConstRef.of(value.getAsLong());
            } else if ((lType instanceof RealType) && (rType instanceof RealType)) {
                OptionalDouble value = doubleValue(operation, converter().getDouble(left), converter().getDouble(right));
                return ConstRef.of(value.getAsDouble());
            } else if ((lType instanceof IntType) && (rType instanceof RealType)) {
                OptionalDouble value = doubleValue(operation, converter().getInt(left), converter().getDouble(right));
                return ConstRef.of(value.getAsDouble());
            } else if ((lType instanceof RealType) && (rType instanceof IntType)) {
                OptionalDouble value = doubleValue(operation, converter().getDouble(left), converter().getInt(right));
                return ConstRef.of(value.getAsDouble());
            } else if ((lType instanceof BoolType) && (rType instanceof BoolType)) {
                Boolean value = booleanValue(operation, converter().getBoolean(left), converter().getBoolean(right));
                return ConstRef.of(value);
            } else {
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprBinaryOp is not supported for this type"));
            }
        }
    }


    default OptionalLong longValue(String operator, long left, long right) {
        switch (operator) {
            case "+":
                return OptionalLong.of(left + right);
            case "-":
                return OptionalLong.of(left - right);
            case "*":
                return OptionalLong.of(left * right);
            case "/":
                return OptionalLong.of(left / right);
            case "<<":
                return OptionalLong.of(left << right);
            case ">>":
                return OptionalLong.of(left >> right);
            case "&":
                return OptionalLong.of(left & right);
            case "|":
                return OptionalLong.of(left | right);
            case "^":
                return OptionalLong.of(left ^ right);
            default:
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Binary operation : \"" + operator + "\" not supported for int type"));
        }
    }

    default Boolean booleanValue(String operator, boolean left, boolean right) {
        switch (operator) {
            case "&":
                return left & right;
            case "&&":
                return left && right;
            case "|":
                return left | right;
            case "||":
                return left || right;
            case "^":
                return left ^ right;
            default:
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Binary operation : \"" + operator + "\" not supported for boolean type."));
        }
    }

    default OptionalDouble doubleValue(String operator, double left, double right) {
        switch (operator) {
            case "+":
                return OptionalDouble.of(left + right);
            case "-":
                return OptionalDouble.of(left - right);
            case "*":
                return OptionalDouble.of(left * right);
            case "/":
                return OptionalDouble.of(left / right);
            default:
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Binary operation : \"" + operator + "\" not supported for real type."));
        }
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
        } else {
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
        RefView value;

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
        RefView operand = evaluate(expression.getOperand(), environment);
        String operation = expression.getOperation();
        Type type = types().type(expression.getOperand());
        switch (operation) {
            case "-": {
                if (type instanceof IntType) {
                    return ConstRef.of(operand.getLong());
                } else if (type instanceof RealType) {
                    return ConstRef.of(-operand.getDouble());
                } else {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Expression type on operation unary \"-\" is not supported."));
                }
            }
            case "~": {
                if (type instanceof IntType) {
                    ConstRef.of(~operand.getLong());
                } else {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Expression type on operation unary \"~\" is not supported."));
                }
            }
            case "!": {
                if (type instanceof BoolType) {
                    ConstRef.of(!operand.getBoolean());
                } else {
                    throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "Expression type on operation unary \"!\" is supported only on boolean types."));
                }
            }
            default:
                throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "ExprUnaryOp is not supported"));
        }
    }

    default RefView evaluate(ExprVariable expression, Environment environment) {
        VarDecl decl = declarations().declaration(expression);
        if (decl.isExternal()) {
            throw new CompilationException(new Diagnostic(Diagnostic.Kind.ERROR, "External ExprVariable is not supported."));
        }

        if(environment.getMemory().getGlobal(decl) != null){
            return environment.getMemory().getGlobal(decl);
        }

        if(environment.getMemory().getLocal(decl) != null){
            return environment.getMemory().getLocal(decl);
        }

        return stack().peek(0);
    }


}
