package se.lth.cs.tycho.analysis.type;

import java.math.BigInteger;
import java.util.Optional;
import java.util.OptionalInt;

import javarag.Bottom;
import javarag.Circular;
import javarag.Module;
import javarag.Synthesized;
import se.lth.cs.tycho.analysis.name.NameAnalysis;
import se.lth.cs.tycho.analysis.name.PortAnalysis;
import se.lth.cs.tycho.analysis.types.BottomType;
import se.lth.cs.tycho.analysis.types.IntType;
import se.lth.cs.tycho.analysis.types.ListType;
import se.lth.cs.tycho.analysis.types.SimpleType;
import se.lth.cs.tycho.analysis.types.Type;
import se.lth.cs.tycho.analysis.value.ConstantEvaluation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.messages.util.Result;

public class TypeAnalysis extends Module<TypeAnalysis.Attributes> {

	public interface Attributes extends Declarations, NameAnalysis.Declarations, ConstantEvaluation.Declarations,
			PortAnalysis.Declarations {
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

	public Type type(ExprInput input) {
		PortDecl port = e().portDeclaration(input.getPort());
		Type type;
		if (port.getType() == null) {
			type = new BottomType();
		} else {
			type = e().type(port.getType());
		}
		if (input.hasRepeat()) {
			int size = input.getRepeat();
			return new ListType(type, size);
		} else {
			return type;
		}
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
			Result<VarDecl> decl = e().variableDeclaration(func.getVariable());
			if (decl.isSuccess()) {
				return e().type(((ExprLambda) decl.get().getValue()).getReturnType());
			} else {
				String funcName = func.getVariable().getName();
				switch (funcName) {
				case "$BinaryOperation...":
					Optional<Object> from = e().constant(apply.getArgs().get(0));
					Optional<Object> to = e().constant(apply.getArgs().get(1));
					if (from.isPresent() && from.get() instanceof Integer && to.isPresent() && to.get() instanceof Integer) {
						return new ListType(new IntType(true), ((Integer) to.get()) - ((Integer) from.get()) + 1);
					}
				case "$BinaryOperation.+":
				case "$BinaryOperation.-":
					IntType left = (IntType) e().type(apply.getArgs().get(0));
					IntType right = (IntType) e().type(apply.getArgs().get(1));
					return intArithOp(funcName, left, right);
				}
			}
		}
		return new BottomType();
	}

	private Type intArithOp(String op, IntType left, IntType right) {
		IntType lub = (IntType) left.leastUpperBound(right);
		if (left.hasSize() && right.hasSize()) {
			switch (op) {
			case "$BinaryOperation.+":
				return lub.hasSize() ? new IntType(lub.isSigned(), lub.getSize()) : lub;
			case "$BinaryOperation.-":
				return lub.hasSize() ? new IntType(lub.isSigned(), lub.getSize()) : lub;
			}
		}
		return lub;
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

	public Type type(ExprIndexer indexer) {
		Type structure = e().type(indexer.getStructure());
		if (structure instanceof ListType) {
			return ((ListType) structure).getElementType();
		} else {
			return new BottomType();
		}
	}

	public Type type(ExprIf ifExpr) {
		Type thenType = e().type(ifExpr.getThenExpr());
		Type elseType = e().type(ifExpr.getElseExpr());
		return thenType.greatestLowerBound(elseType);
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
		case "List":
			Optional<Object> listSize = type.getValueParameters()
					.stream()
					.filter(p -> p.getName().equals("size"))
					.findFirst()
					.map(Parameter::getValue)
					.flatMap(e()::constant);
			Optional<Type> element = type.getTypeParameters()
					.stream()
					.filter(p -> p.getName().equals("type"))
					.findFirst()
					.map(Parameter::getValue)
					.map(e()::type);
			if (listSize.isPresent()) {
				return new ListType(element.get(), (Integer) listSize.get());
			} else {
				return new ListType(element.get());
			}
		default:
			return new BottomType();
		}
	}
}
