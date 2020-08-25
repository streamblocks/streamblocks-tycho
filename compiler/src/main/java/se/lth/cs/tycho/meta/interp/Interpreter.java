package se.lth.cs.tycho.meta.interp;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ModuleKey;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprComprehension;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprNth;
import se.lth.cs.tycho.ir.expr.ExprSet;
import se.lth.cs.tycho.ir.expr.ExprTuple;
import se.lth.cs.tycho.ir.expr.ExprTypeAssertion;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlternative;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternLiteral;
import se.lth.cs.tycho.ir.expr.pattern.PatternTuple;
import se.lth.cs.tycho.ir.expr.pattern.PatternWildcard;
import se.lth.cs.tycho.meta.interp.op.Binary;
import se.lth.cs.tycho.meta.interp.op.Unary;
import se.lth.cs.tycho.meta.interp.op.operator.Operator;
import se.lth.cs.tycho.meta.interp.value.*;
import se.lth.cs.tycho.meta.interp.value.ValueLong;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Module
public interface Interpreter {

    ModuleKey<Interpreter> key = task-> MultiJ.from(Interpreter.class)
            .bind("variables").to(task.getModule(VariableDeclarations.key))
            .bind("types").to(task.getModule(TypeScopes.key))
            .bind("unary").to(MultiJ.from(Unary.class).instance())
            .bind("binary").to(MultiJ.from(Binary.class).instance())
            .instance();

    @Binding(BindingKind.INJECTED)
    VariableDeclarations variables();

    @Binding(BindingKind.INJECTED)
    TypeScopes types();

    @Binding(BindingKind.INJECTED)
    Unary unary();

    @Binding(BindingKind.INJECTED)
    Binary binary();

    default Value apply(Expression expr) {
        Environment env = new Environment();
        return eval(expr, env);
    }

    default Value eval(Expression expr, Environment env) {
        return ValueUndefined.undefined();
    }

    default Value eval(ExprApplication expr, Environment env) {
        Value value = eval(expr.getFunction(), env);
        if (!(value instanceof ValueLambda)) {
            return ValueUndefined.undefined();
        }

        ValueLambda lambda = (ValueLambda) value;
        if (expr.getArgs().size() +
                lambda.parameters().stream()
                        .filter(param -> param.defaultValue().isPresent())
                        .count() > lambda.parameters().size()) {
            return ValueUndefined.undefined();
        }

        Map<String, Value> bindings = new HashMap<>();
        for (int i = 0; i < lambda.parameters().size(); ++i) {
            ValueParameter param = lambda.parameters().get(i);
            if (i < expr.getArgs().size()) {
                bindings.put(param.name(), eval(expr.getArgs().get(i), env));
            } else {
                bindings.put(param.name(), param.defaultValue().get());
            }
        }

        return eval(lambda.body(), env.with(bindings));
    }

    default Value eval(ExprBinaryOp expr, Environment env) {
        Value lhs = eval(expr.getOperands().get(0), env);
        Value rhs = eval(expr.getOperands().get(1), env);
        Operator operator = Operator.of(expr.getOperations().get(0));
        return binary().apply(operator, lhs, rhs);
    }

    default Value eval(ExprCase expr, Environment env) {
        Value scrutinee = eval(expr.getScrutinee(), env);
        if (scrutinee == ValueUndefined.undefined()) {
            return ValueUndefined.undefined();
        }
        for (ExprCase.Alternative alternative : expr.getAlternatives()) {
            Map<String, Value> bindings = new HashMap<>();
            if (match(scrutinee, alternative.getPattern(), env, bindings)) {
                Environment newEnv = env.with(bindings);
                if (alternative.getGuards().stream()
                        .map(guard -> eval(guard, newEnv))
                        .allMatch(guard -> guard instanceof ValueBool && ((ValueBool) guard).bool())) {
                    return eval(alternative.getExpression(), newEnv);
                }
            }
        }
        return ValueUndefined.undefined();
    }

    default boolean match(Value scrutinee, Pattern pattern, Environment env, Map<String, Value> bindings) {
        return false;
    }

    default boolean match(Value scrutinee, PatternAlias pattern, Environment env, Map<String, Value> bindings) {
        return scrutinee.equals(eval(pattern.getExpression(), env)) && match(scrutinee, pattern.getAlias(), env, bindings);
    }

    default boolean match(Value scrutinee, PatternAlternative pattern, Environment env, Map<String, Value> bindings) {
        return pattern.getPatterns().stream().anyMatch(p -> match(scrutinee, p, env, bindings));
    }

    default boolean match(Value scrutinee, PatternBinding pattern, Environment env, Map<String, Value> bindings) {
        bindings.put(pattern.getDeclaration().getName(), scrutinee);
        return true;
    }

    default boolean match(ValueProduct scrutinee, PatternDeconstruction pattern, Environment env, Map<String, Value> bindings) {
        Optional<TypeDecl> decl = types().construction(pattern);
        if (!decl.isPresent()) {
            return false;
        }

        AlgebraicTypeDecl algebraicDecl = (AlgebraicTypeDecl) decl.get();
        if (!(algebraicDecl instanceof ProductTypeDecl)) {
            return false;
        }

        ProductTypeDecl product = (ProductTypeDecl) algebraicDecl;
        return scrutinee.name().equals(product.getName())
                && product.getFields().size() == pattern.getPatterns().size()
                && scrutinee.fields().size() == pattern.getPatterns().size()
                && IntStream.range(0, pattern.getPatterns().size())
                .allMatch(i -> match(scrutinee.fields().get(i), pattern.getPatterns().get(i), env, bindings));
    }

    default boolean match(ValueSum scrutinee, PatternDeconstruction pattern, Environment env, Map<String, Value> bindings) {
        Optional<TypeDecl> decl = types().construction(pattern);
        if (!decl.isPresent()) {
            return false;
        }

        AlgebraicTypeDecl algebraicDecl = (AlgebraicTypeDecl) decl.get();
        if (!(algebraicDecl instanceof SumTypeDecl)) {
            return false;
        }

        SumTypeDecl.VariantDecl variant = ((SumTypeDecl) algebraicDecl).getVariants().stream().filter(v -> v.getName().equals(scrutinee.name())).findAny().get();
        return variant.getFields().size() == pattern.getPatterns().size()
                && scrutinee.fields().size() == pattern.getPatterns().size()
                && IntStream.range(0, pattern.getPatterns().size())
                .allMatch(i -> match(scrutinee.fields().get(i), pattern.getPatterns().get(i), env, bindings));
    }

    default boolean match(Value scrutinee, PatternExpression pattern, Environment env, Map<String, Value> bindings) {
        return scrutinee.equals(eval(pattern.getExpression(), env));
    }

    default boolean match(ValueList scrutinee, PatternList pattern, Environment env, Map<String, Value> bindings) {
        return scrutinee.elements().size() >= pattern.getPatterns().size()
                && IntStream
                .range(0, Math.min(scrutinee.elements().size(), pattern.getPatterns().size()))
                .allMatch(i -> match(scrutinee.elements().get(i), pattern.getPatterns().get(i), env, bindings));
    }

    default boolean match(Value scrutinee, PatternLiteral pattern, Environment env, Map<String, Value> bindings) {
        return scrutinee.equals(eval(pattern.getLiteral(), env));
    }

    default boolean match(ValueTuple scrutinee, PatternTuple pattern, Environment env, Map<String, Value> bindings) {
        return scrutinee.elements().size() == pattern.getPatterns().size()
                && IntStream.range(0, pattern.getPatterns().size())
                .allMatch(i -> match(scrutinee.elements().get(i), pattern.getPatterns().get(i), env, bindings));
    }

    default boolean match(Value scrutinee, PatternWildcard pattern, Environment env, Map<String, Value> bindings) {
        return true;
    }

    default Value eval(ExprComprehension expr, Environment env) {
        Generator generator = expr.getGenerator();

        Value value = eval(generator.getCollection(), env);
        if (!(value instanceof ValueList)) {
            return ValueUndefined.undefined();
        }

        List<Value> generated = new ArrayList<>();

        ValueList list = (ValueList) value;
        for (int i = 0; i < list.elements().size(); i += generator.getVarDecls().size()) {
            Map<String, Value> bindings = new HashMap<>();
            for (int j = 0; j < generator.getVarDecls().size(); ++j) {
                bindings.put(generator.getVarDecls().get(i).getName(), eval(generator.getVarDecls().get(i).getValue(), env));
            }

            Environment newEnv = env.with(bindings);
            if (expr.getFilters().stream().map(filter -> eval(filter, newEnv)).allMatch(v -> (v instanceof ValueBool) && ((ValueBool) v).bool())) {
                generated.add(eval(expr.getCollection(), newEnv));
            }
        }

        return new ValueList(generated.stream().flatMap(v -> {
            if (v instanceof ValueList) {
                return ((ValueList) v).elements().stream();
            } else {
                return Stream.of(v);
            }
        }).collect(Collectors.toList()));
    }

    default Value eval(ExprField expr, Environment env) {
        Value structure = eval(expr.getStructure(), env);
        if (structure instanceof ValueProduct) {
            return ((ValueProduct) structure).fields().stream()
                    .filter(parameter -> Objects.equals(parameter.name(), expr.getField().getName()))
                    .map(ValueField::value)
                    .findAny()
                    .orElse(ValueUndefined.undefined());
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprGlobalVariable expr, Environment env) {
        Optional<Value> cached = env.get(expr.getGlobalName().toString());
        if (cached.isPresent()) {
            return cached.get();
        }

        Value value = ValueUndefined.undefined();

        VarDecl decl = variables().declaration(expr);
        if (!decl.isExternal() && decl.getValue() != null) {
            value = eval(decl.getValue(), env);
        }
        env.put(expr.getGlobalName().toString(), value);

        return value;
    }

    default Value eval(ExprIf expr, Environment env) {
        Value condition = eval(expr.getCondition(), env);
        if (condition instanceof ValueBool) {
            return ((ValueBool) condition).bool() ? eval(expr.getThenExpr(), env) : eval(expr.getElseExpr(), env);
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprIndexer expr, Environment env) {
        Value value = eval(expr.getStructure(), env);
        Value index = eval(expr.getIndex(), env);
        if (value instanceof ValueList
                && index instanceof ValueLong
                && ((ValueLong) index).value() > -1
                && ((ValueLong) index).value() < ((ValueList) value).elements().size()) {
            return ((ValueList) value).elements().get((int) ((ValueLong) index).value());
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprLambda expr, Environment env) {
        return new ValueLambda(expr.getValueParameters().map(param ->
                new ValueParameter(param.getType(), param.getName(), param.getDefaultValue() == null ? Optional.empty() : Optional.of(eval(param.getDefaultValue(), env)))
        ), expr.getBody(), expr.getReturnType());
    }

    default Value eval(ExprLet expr, Environment env) {
        Map<String, Value> bindings = new HashMap<>();
        for (VarDecl decl : expr.getVarDecls()) {
            if (!decl.isExternal() && decl.isConstant()) {
                bindings.put(decl.getName(), eval(decl.getValue(), env));
            }
        }
        return eval(expr.getBody(), env.with(bindings));
    }

    default Value eval(ExprList expr, Environment env) {
        List<Value> elements = expr.getElements().map(elem -> eval(elem, env));
        if (elements.size() > 0 && elements.subList(1, elements.size()).stream().allMatch(e -> e.getClass().equals(elements.get(0).getClass()))) {
            return new ValueList(elements);
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprSet expr, Environment env) {
        List<Value> elements = expr.getElements().map(elem -> eval(elem, env));
        if (elements.size() > 0 && elements.subList(1, elements.size()).stream().allMatch(e -> e.getClass().equals(elements.get(0).getClass()))) {
            return new ValueSet(new HashSet<>(elements));
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprLiteral expr, Environment env) {
        switch (expr.getKind()) {
            case False:
            case True:
                return new ValueBool(Boolean.valueOf(expr.getText()));
            case Integer:
                try {
                    String text = expr.getText();
                    int radix = 10;
                    if (text.startsWith("0x")) {
                        text = text.substring(2);
                        radix = 16;
                        return new ValueLong(Long.parseUnsignedLong(text, radix));
                    }
                    return new ValueLong(Long.parseLong(text, radix));
                } catch (NumberFormatException e) {
                    return ValueUndefined.undefined();
                }
            case Char:
                return new ValueChar(Character.valueOf(expr.getText().charAt(0)));
            case String:
                return new ValueString(expr.getText());
            case Real:
                return new ValueReal(Double.valueOf(expr.getText()));
            default:
                return ValueUndefined.undefined();
        }
    }

    default Value eval(ExprNth expr, Environment env) {
        Value value = eval(expr.getStructure(), env);
        Integer idx = expr.getNth().getNumber();
        if (value instanceof ValueTuple && idx > 0 && idx <= ((ValueTuple) value).elements().size()) {
            return ((ValueTuple) value).elements().get(idx - 1);
        }
        return ValueUndefined.undefined();
    }

    default Value eval(ExprTuple expr, Environment env) {
        return new ValueTuple(expr.getElements().map(elem -> eval(elem, env)));
    }

    default Value eval(ExprTypeAssertion expr, Environment env) {
        throw new RuntimeException("Not implemented yet");
    }

    default Value eval(ExprTypeConstruction expr, Environment env) {
        Optional<TypeDecl> decl = types().construction(expr);
        if (!decl.isPresent()) {
            return ValueUndefined.undefined();
        }

        AlgebraicTypeDecl algebraicDecl = (AlgebraicTypeDecl) decl.get();
        if (algebraicDecl instanceof ProductTypeDecl) {
            ProductTypeDecl product = (ProductTypeDecl) algebraicDecl;
            if (expr.getArgs().size() == product.getFields().size()) {
                return new ValueProduct(expr.getConstructor(), IntStream
                        .range(0, expr.getArgs().size())
                        .mapToObj(i -> new ValueField(product.getFields().get(i).getName(), eval(expr.getArgs().get(i), env)))
                        .collect(Collectors.toList()));
            }
        } else {
            SumTypeDecl sum = (SumTypeDecl) algebraicDecl;
            SumTypeDecl.VariantDecl variant = sum.getVariants().stream().filter(v -> v.getName().equals(expr.getConstructor())).findAny().get();
            if (expr.getArgs().size() == variant.getFields().size()) {
                return new ValueSum(expr.getConstructor(), IntStream
                        .range(0, expr.getArgs().size())
                        .mapToObj(i -> new ValueField(variant.getFields().get(i).getName(), eval(expr.getArgs().get(i), env)))
                        .collect(Collectors.toList()));
            }
        }

        return ValueUndefined.undefined();
    }

    default Value eval(ExprUnaryOp expr, Environment env) {
        Value operand = eval(expr.getOperand(), env);
        Operator operator = Operator.of(expr.getOperation());
        return unary().apply(operator, operand);
    }

    default Value eval(ExprVariable expr, Environment env) {
        Optional<Value> value = env.get(expr.getVariable().getName());
        if (value.isPresent()) {
            return value.get();
        }

        Value result = ValueUndefined.undefined();

        VarDecl decl = variables().declaration(expr);
        if (decl.isConstant() && decl.getValue() != null) {
            result = eval(decl.getValue(), env);
        }
        env.put(expr.getVariable().getName(), result);

        return result;
    }
}
