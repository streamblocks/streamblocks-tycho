package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.meta.interp.Environment;
import se.lth.cs.tycho.meta.interp.Interpreter;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueLong;
import se.lth.cs.tycho.meta.interp.value.ValueUndefined;

import java.util.Optional;
import java.util.OptionalLong;

@Module
public interface ConstantEvaluator {

    ModuleKey<ConstantEvaluator> key = task -> MultiJ.from(ConstantEvaluator.class)
            .bind("interpreter").to(task.getModule(Interpreter.key))
            .instance();

    @Binding(BindingKind.INJECTED)
    Interpreter interpreter();


    default OptionalLong intValue(Expression e) {
        Environment environment = new Environment();

        Value value = interpreter().eval(e, environment);

        if (value instanceof ValueUndefined) {
            return OptionalLong.empty();
        } else {
            return OptionalLong.of(((ValueLong) value).value());
        }
    }


    default Optional<Boolean> boolValue(Expression e) {
        Environment environment = new Environment();

        Value value = interpreter().eval(e, environment);

        if (value instanceof ValueUndefined) {
            return Optional.empty();
        } else {
            return Optional.of(((ValueBool) value).bool());
        }
    }

}
