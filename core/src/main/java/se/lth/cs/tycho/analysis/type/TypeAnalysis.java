package se.lth.cs.tycho.analysis.type;

import java.math.BigInteger;
import java.util.Optional;
import java.util.OptionalInt;

import javarag.Bottom;
import javarag.Circular;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.analysis.name.NameAnalysis;
import se.lth.cs.tycho.analysis.types.BottomType;
import se.lth.cs.tycho.analysis.types.IntType;
import se.lth.cs.tycho.analysis.types.ListType;
import se.lth.cs.tycho.analysis.types.SimpleType;
import se.lth.cs.tycho.analysis.types.Type;
import se.lth.cs.tycho.analysis.value.ConstantEvaluation;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.messages.util.Result;

public class TypeAnalysis extends Module<TypeAnalysis.Attributes> {

	public interface Attributes extends Declarations, NameAnalysis.Declarations, ConstantEvaluation.Declarations {
	}

	public interface Declarations {
		@Synthesized
		@Circular
		Type type(Expression expr);

		@Synthesized
		@Circular
		Type type(VarDecl decl);

		@Synthesized
		@Circular
		Type type(TypeExpr type);

		@Synthesized
		@Circular
		Type type(LValue lvalue);

		@Synthesized
		@Circular
		Type type(Variable var);
	}

	@Bottom("type")
	public Type typeBottom(IRNode node) {
		return new BottomType();
	}

	public Type type(VarDecl decl) {
		if (decl.getType() != null) {
			return e().type(decl.getType());
		} else if (decl.getValue() != null) {
			return e().type(decl.getValue());
		} else {
			return new BottomType();
		}
	}

	public Type type(Expression expr) {
		return new BottomType();
	}

	public Type type(ExprVariable var) {
		return e().type(var.getVariable());
	}
	
	public Type type(ExprList list) {
		Type elements = new BottomType();
		for (Expression e : list.getElements()) {
			elements = elements.leastUpperBound(e().type(e));
		}
		if (list.getGenerators().isEmpty()) {
			return new ListType(elements, list.getElements().size());
		} else if (list.getGenerators().size() == 1 && list.getGenerators().get(0).getFilters().isEmpty()) {
			Expression coll = list.getGenerators().get(0).getCollectionExpr();
			Type collType = e().type(coll);
			if (collType instanceof ListType) {
				OptionalInt size = ((ListType) collType).getSize();
				if (size.isPresent()) {
					return new ListType(elements, list.getElements().size() * size.getAsInt());
				}
			}
		}
		return new ListType(elements);
	}
	
	public Type type(ExprApplication apply) {
		if (apply.getFunction() instanceof ExprVariable) {
			ExprVariable func = (ExprVariable) apply.getFunction();
			switch (func.getVariable().getName()) {
			case "$BinaryOperator...":
				Optional<Object> from = e().constant(apply.getArgs().get(0));
				Optional<Object> to = e().constant(apply.getArgs().get(1));
				if (from.isPresent() && from.get() instanceof Integer && to.isPresent() && to.get() instanceof Integer) {
					return new ListType(new IntType(true), ((Integer) to.get()) - ((Integer) from.get()));
				}
			}
		}
		return new BottomType();
	}

	public Type type(ExprLiteral lit) {
		switch (lit.getKind()) {
		case Integer:
			int value = Integer.valueOf(lit.getText());
			boolean signed = value < 0;
			int size = signed ? BigInteger.valueOf(value).bitLength() + 1 : BigInteger.valueOf(value).bitLength();
			return new IntType(signed, size);
		case True:
			return new SimpleType("bool");
		case False:
			return new SimpleType("bool");
		case Char:
		case String:
		case Null:
		case Real:
		case Function:
		default:
			return new BottomType();
		}
	}

	public Type type(Variable variable) {
		Result<VarDecl> decl = e().variableDeclaration(variable);
		if (decl.isSuccess()) {
			return e().type(decl.get());
		} else {
			return new BottomType();
		}
	}

	public Type type(LValueVariable lvalue) {
		return e().type(lvalue.getVariable());
	}

	public Type type(TypeExpr type) {
		boolean signed = true;
		switch (type.getName()) {
		case "uint":
			signed = false;
		case "int":
			Optional<Object> size = type.getValueParameters()
					.stream()
					.filter(p -> p.getName().equals("size"))
					.findFirst()
					.map(Parameter::getValue)
					.flatMap(e()::constant);
			if (size.isPresent() && size.get() instanceof Integer) {
				int s = (Integer) size.get();
				return new IntType(signed, s);
			} else {
				if (type.getValueParameters().isEmpty()) {
					return new IntType(signed);					
				} else {
					return new IntType(signed);
				}
			}
		case "bool":
			return new SimpleType("bool");
		default:
			return new BottomType();
		}
	}
}
