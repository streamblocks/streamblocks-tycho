package se.lth.cs.tycho.meta.interp.value.util;

import org.multij.Module;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprTuple;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.interp.value.*;
import se.lth.cs.tycho.meta.interp.value.ValueLong;

import java.util.stream.Collectors;

@Module
public interface Convert {

	default Expression apply(Value value) {
		return null;
	}

	default Expression apply(ValueAlgebraic value) {
		return new ExprTypeConstruction(value.name(), ImmutableList.empty(), ImmutableList.empty(), value.fields().stream().map(this::apply).collect(Collectors.toList()));
	}

	default Expression apply(ValueBool value) {
		return new ExprLiteral(value.bool() ? ExprLiteral.Kind.True : ExprLiteral.Kind.False, String.valueOf(value.bool()));
	}

	default Expression apply(ValueChar value) {
		return new ExprLiteral(ExprLiteral.Kind.Char, String.valueOf(value.character()));
	}

	default Expression apply(ValueField value) {
		return apply(value.value());
	}

	default Expression apply(ValueLong value) {
		return new ExprLiteral(ExprLiteral.Kind.Integer, String.valueOf(value.value()));
	}

	default Expression apply(ValueLambda value) {
		return new ExprLambda(value.parameters().stream().map(param -> new ParameterVarDecl(ImmutableList.empty(), param.type(), param.name(), apply(param.defaultValue().orElse(ValueUndefined.undefined())))).collect(Collectors.toList()), value.body(), value.type());
	}

	default Expression apply(ValueList value) {
		return new ExprList(value.elements().stream().map(this::apply).collect(Collectors.toList()));
	}

	default Expression apply(ValueReal value) {
		return new ExprLiteral(ExprLiteral.Kind.Real, String.valueOf(value.real()));
	}

	default Expression apply(ValueString value) {
		return new ExprLiteral(ExprLiteral.Kind.String, value.string());
	}

	default Expression apply(ValueTuple value) {
		return new ExprTuple(value.elements().stream().map(this::apply).collect(Collectors.toList()));
	}
}
