package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.AliasType;
import se.lth.cs.tycho.type.MapType;
import se.lth.cs.tycho.type.SetType;
import se.lth.cs.tycho.type.StringType;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

@Module
public interface SizeOf {

	@Binding(BindingKind.INJECTED)
	Backend backend();
	default Code code() {
		return backend().code();
	}

	default String evaluate(Type type, String variable) {
		return String.format("sizeof(%s)", code().type(type));
	}

	default String evaluate(AlgebraicType type, String variable) {
		return String.format("size_%s(%s)", code().type(type), variable);
	}

	default String evaluate(SetType type, String variable) {
		return String.format("size_%s(%s)", code().type(type), variable);
	}

	default String evaluate(MapType type, String variable) {
		return String.format("size_%s(%s)", code().type(type), variable);
	}

	default String evaluate(TupleType type, String variable) {
		return String.format("size_%s(%s)", code().type(type), variable);
	}

	default String evaluate(StringType type, String variable) {
		return String.format("size_%s(%s)", code().type(type), variable);
	}

	default String evaluate(AliasType type, String variable) {
		return evaluate(type.getConcreteType(), variable);
	}
}
