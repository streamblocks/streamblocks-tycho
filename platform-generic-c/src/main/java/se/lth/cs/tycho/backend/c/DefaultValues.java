package se.lth.cs.tycho.backend.c;

import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.AliasType;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.CallableType;
import se.lth.cs.tycho.type.CharType;
import se.lth.cs.tycho.type.IntType;
import se.lth.cs.tycho.type.ListType;
import se.lth.cs.tycho.type.MapType;
import se.lth.cs.tycho.type.RealType;
import se.lth.cs.tycho.type.SetType;
import se.lth.cs.tycho.type.StringType;
import se.lth.cs.tycho.type.TupleType;
import se.lth.cs.tycho.type.Type;

import java.util.List;

@Module
public interface DefaultValues {
	String defaultValue(Type type);
	default String defaultValue(CallableType t) {
		return "{ NULL, NULL }";
	}
	default String defaultValue(BoolType t) {
		return "false";
	}
	default String defaultValue(CharType t) {
		return "0";
	}
	default String defaultValue(RealType t) {
		return "0.0";
	}
	default String defaultValue(IntType t) {
		return "0";
	}
	default String defaultValue(ListType t) {
		if (t.getSize().isPresent()) {
			StringBuilder builder = new StringBuilder();
			String element = defaultValue(t.getElementType());

			// If statement added by Gareth Callanan to fix compile error with nested lists. (Part 1 of 2)
			if(t.getElementType() instanceof ListType){
				builder.append("{");
			}

			builder.append("{");
			for (int i = 0; i < t.getSize().getAsLong(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(element);
			}
			builder.append("}");

			// If statement added by Gareth Callanan to fix compile error with nested lists. (Part 2 of 2)
			if(t.getElementType() instanceof ListType){
				builder.append("}");
			}

			return builder.toString();
		} else {
			throw new UnsupportedOperationException("Not implemented");
		}
	}
	default String defaultValue(AlgebraicType t) {
		return "NULL";
	}
	default String defaultValue(AliasType t) {
		return defaultValue(t.getConcreteType());
	}
	default String defaultValue(TupleType t) {
		return "NULL";
	}
	default String defaultValue(SetType t) {
		return "NULL";
	}
	default String defaultValue(MapType t) {
		return "NULL";
	}
	default String defaultValue(StringType t) {
		return "NULL";
	}
}
