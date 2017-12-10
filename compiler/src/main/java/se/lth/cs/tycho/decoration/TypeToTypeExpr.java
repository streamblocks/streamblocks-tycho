package se.lth.cs.tycho.decoration;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.type.FunctionTypeExpr;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.ProcedureTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.types.BoolType;
import se.lth.cs.tycho.types.IntType;
import se.lth.cs.tycho.types.LambdaType;
import se.lth.cs.tycho.types.ListType;
import se.lth.cs.tycho.types.ProcType;
import se.lth.cs.tycho.types.RealType;
import se.lth.cs.tycho.types.Type;

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

	}
}