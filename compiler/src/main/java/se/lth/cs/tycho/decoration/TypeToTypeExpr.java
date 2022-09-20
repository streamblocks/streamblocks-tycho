package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.type.*;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.type.*;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public final class TypeToTypeExpr {
	private TypeToTypeExpr() {}

	private static final Conversion conversion = MultiJ.instance(Conversion.class);

	public static TypeExpr<?> convert(Type type) {
		return conversion.convert(type);
	}

	@Module
	interface Conversion {
		TypeExpr<?> convert(Type type);

		default NominalTypeExpr convert(BoolType type) {
			return new NominalTypeExpr("bool");
		}

		default NominalTypeExpr convert(TensorType type){
			return new NominalTypeExpr("Tensor");
		}

		default NominalTypeExpr convert(TorchIntArrayRef type){
			return new NominalTypeExpr("IntArrayRef");
		}

		default ValueParameter intParameter(String name, int value) {
			return new ValueParameter(name, new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(value)));
		}

		default NominalTypeExpr convert(IntType type) {
			String name = type.isSigned() ? "int" : "uint";
			ImmutableList<ValueParameter> parameter;
			OptionalInt optionalSize = type.getSize();
			if (optionalSize.isPresent()) {
				parameter = ImmutableList.of(intParameter("size", optionalSize.getAsInt()));
			} else {
				parameter = ImmutableList.empty();
			}
			return new NominalTypeExpr(name, ImmutableList.empty(), parameter);
		}

		default NominalTypeExpr convert(RealType type) {
			String name;
			if (type.getSize() == 32) {
				name = "float";
			} else if (type.getSize() == 64) {
				name = "double";
			} else {
				throw new AssertionError();
			}
			return new NominalTypeExpr(name, ImmutableList.empty(), ImmutableList.empty());
		}

		default NominalTypeExpr convert(CharType type) {
			return new NominalTypeExpr("char");
		}

		default FunctionTypeExpr convert(LambdaType type) {
			List<TypeExpr> parameterTypes = type.getParameterTypes().stream()
					.map(this::convert)
					.collect(Collectors.toList());
			return new FunctionTypeExpr(parameterTypes, convert(type.getReturnType()));
		}

		default NominalTypeExpr convert(ListType type) {
			TypeParameter elementType = new TypeParameter("type", convert(type.getElementType()));
			ImmutableList<ValueParameter> size;
			OptionalInt optionalSize = type.getSize();
			if (optionalSize.isPresent()) {
				size = ImmutableList.of(intParameter("size", optionalSize.getAsInt()));
			} else {
				size = ImmutableList.empty();
			}
			return new NominalTypeExpr("List", ImmutableList.of(elementType), size);
		}

		default ProcedureTypeExpr convert(ProcType type) {
			List<TypeExpr> parameterTypes = type.getParameterTypes().stream()
					.map(this::convert)
					.collect(Collectors.toList());
			return new ProcedureTypeExpr(parameterTypes);
		}

		default NominalTypeExpr convert(AlgebraicType type) {
			return new NominalTypeExpr(type.getName());
		}

		default NominalTypeExpr convert(AliasType type) {
			return new NominalTypeExpr(type.getName());
		}

		default TupleTypeExpr convert(TupleType type) {
			return new TupleTypeExpr(type.getTypes().map(this::convert));
		}

		default NominalTypeExpr convert(SetType type) {
			TypeParameter elementType = new TypeParameter("type", convert(type.getElementType()));
			return new NominalTypeExpr("Set", ImmutableList.of(elementType), ImmutableList.empty());
		}

		default NominalTypeExpr convert(MapType type) {
			TypeParameter keyType = new TypeParameter("key", convert(type.getKeyType()));
			TypeParameter valueType = new TypeParameter("value", convert(type.getValueType()));
			return new NominalTypeExpr("Map", ImmutableList.of(keyType, valueType), ImmutableList.empty());
		}

		default NominalTypeExpr convert(StringType type) {
			return new NominalTypeExpr("String", ImmutableList.empty(), ImmutableList.empty());
		}
	}
}
