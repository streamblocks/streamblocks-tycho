package se.lth.cs.tycho.phases.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.phases.TreeShadow;
import se.lth.cs.tycho.types.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Types {

	ModuleKey<Types> key = new ModuleKey<Types>() {
		@Override
		public Types createInstance(CompilationTask unit, AttributeManager manager) {
			return MultiJ.from(Implementation.class)
					.bind("names").to(manager.getAttributeModule(Names.key, unit))
					.bind("constants").to(manager.getAttributeModule(Constants.key, unit))
					.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
					.bind("globalNames").to(manager.getAttributeModule(GlobalNames.key, unit))
					.instance();
		}
	};

	Type declaredType(VarDecl decl);
	Type type(Expression expr);
	Type lvalueType(LValue lvalue);
	Type declaredPortType(PortDecl port);
	Type portType(Port port);
	Type portTypeRepeated(Port port, Expression repeat);

	@Module
	interface Implementation extends Types {


		@Binding(BindingKind.INJECTED)
		Names names();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Binding(BindingKind.INJECTED)
		Constants constants();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding
		default Map<Expression, Type> typeMap() {
			return new ConcurrentHashMap<>();
		}

		default Type type(Expression e) {
			if (typeMap().containsKey(e)) {
				return typeMap().get(e);
			} else {
				Type t = computeType(e);
				Type old = typeMap().putIfAbsent(e, t);
				return old != null ? old : t;
			}
		}

		@Binding
		default ThreadLocal<Set<VarDecl>> currentlyComputing() {
			return ThreadLocal.withInitial(HashSet::new);
		}

		@Binding
		default Map<VarDecl, Type> declaredTypeMap() {
			return new ConcurrentHashMap<>();
		}

		default Type declaredType(VarDecl varDecl) {
			if (declaredTypeMap().containsKey(varDecl)) {
				return declaredTypeMap().get(varDecl);
			} else if (currentlyComputing().get().contains(varDecl)) {
				return BottomType.INSTANCE;
			} else {
				currentlyComputing().get().add(varDecl);
				Type t = computeDeclaredType(varDecl);
				currentlyComputing().get().remove(varDecl);
				Type old = declaredTypeMap().putIfAbsent(varDecl, t);
				return old != null ? old : t;
			}
		}

		default Type computeDeclaredType(VarDecl varDecl) {
			if (varDecl.isImport()) {
				return declaredType(globalNames().varDecl(varDecl.getQualifiedIdentifier(), false));
			} else if (varDecl.getType() != null) {
				return convert(varDecl.getType());
			} else if (tree().parent(varDecl) instanceof InputPattern) {
				InputPattern input = (InputPattern) tree().parent(varDecl);
				PortDecl port = names().portDeclaration(input.getPort());
				Type result = convert(port.getType());
				if (input.getRepeatExpr() != null) {
					OptionalInt size = constants().intValue(input.getRepeatExpr());
					return new ListType(result, size);
				} else {
					return result;
				}
			} else if (varDecl.getValue() != null) {
				return type(varDecl.getValue());
			} else {
				return BottomType.INSTANCE;
			}
		}

		@Binding
		default Map<LValue, Type> lvalueTypeMap() {
			return new ConcurrentHashMap<>();
		}

		default Type lvalueType(LValue lvalue) {
			return lvalueTypeMap().computeIfAbsent(lvalue, this::computeLValueType);
		}

		default Type declaredPortType(PortDecl port) {
			return convert(port.getType());
		}

		default Type portType(Port port) {
			return declaredPortType(names().portDeclaration(port));
		}

		default Type portTypeRepeated(Port port, Expression repeat) {
			Type element = portType(port);
			OptionalInt size = constants().intValue(repeat);
			return new ListType(element, size);
		}

		Type computeLValueType(LValue lvalue);

		default Type computeLValueType(LValueVariable var) {
			return declaredType(names().declaration(var.getVariable()));
		}

		default Type computeLValueType(LValueIndexer indexer) {
			Type structureType = computeLValueType(indexer.getStructure());
			if (structureType instanceof ListType) {
				return ((ListType) structureType).getElementType();
			} else {
				return BottomType.INSTANCE;
			}
		}


		default Type convert(TypeExpr t) {
			switch (t.getName()) {
				case "List": {
					Optional<TypeExpr> e = findParameter(t.getTypeParameters(), "type");
					Optional<Type> elements = e.map(this::convert);
					if (elements.isPresent()) {
						Optional<Expression> s = findParameter(t.getValueParameters(), "size");
						if (s.isPresent()) {
							OptionalInt size = constants().intValue(s.get());
							if (size.isPresent()) {
								return new ListType(elements.get(), size);
							}
						}
						return new ListType(elements.get(), OptionalInt.empty());
					}
					return BottomType.INSTANCE;
				}
				case "int": {
					Optional<Expression> s = findParameter(t.getValueParameters(), "size");
					if (s.isPresent()) {
						OptionalInt size = constants().intValue(s.get());
						if (size.isPresent()) {
							return new IntType(size, true);
						} else {
							return BottomType.INSTANCE;
						}
					} else {
						return new IntType(OptionalInt.empty(), true);
					}
				}
				case "uint": {
					Optional<Expression> s = findParameter(t.getValueParameters(), "size");
					if (s.isPresent()) {
						OptionalInt size = constants().intValue(s.get());
						if (size.isPresent()) {
							return new IntType(size, false);
						} else {
							return BottomType.INSTANCE;
						}
					} else {
						return new IntType(OptionalInt.empty(), false);
					}
				}
				case "bool": {
					return BoolType.INSTANCE;
				}
				case "unit": {
					return UnitType.INSTANCE;
				}
				default:
					return BottomType.INSTANCE;
			}
		}

		default <E extends IRNode> Optional<E> findParameter(List<Parameter<E>> parameters, String name) {
			return parameters.stream()
					.filter(param -> param.getName().equals(name))
					.map(Parameter::getValue)
					.findFirst();
		}

		default Type computeType(Expression e) {
			return BottomType.INSTANCE;
		}

		default Type computeType(ExprLiteral e) {
			switch (e.getKind()) {
				case True:
				case False: {
					return BoolType.INSTANCE;
				}
				case Integer: {
					OptionalInt value = constants().intValue(e);
					final int v = value.getAsInt();
					if (v == 0) {
						return new IntType(OptionalInt.of(1), false);
					} else if (v == -1) {
						return new IntType(OptionalInt.of(1), true);
					} else if (v < -1) {
						int size = (int) (Math.log(-v-1)/Math.log(2) + 2);
						return new IntType(OptionalInt.of(size + 1), true);
					} else {
						int size = (int) (Math.log(v)/Math.log(2) + 1);
						return new IntType(OptionalInt.of(size), false);
					}
				}
				default: {
					return BottomType.INSTANCE;
				}
			}
		}

		default Type computeType(ExprList list) {
			Type elementType = list.getElements().stream()
					.map(this::type)
					.reduce(BottomType.INSTANCE, this::leastUpperBound);
			if (list.getGenerators().isEmpty()) {
				return new ListType(elementType, OptionalInt.of(list.getElements().size()));
			} else {
				int size = list.getElements().size();
				for (GeneratorFilter gen : list.getGenerators()) {
					if (!gen.getFilters().isEmpty()) {
						return new ListType(elementType, OptionalInt.empty());
					}
					Type collType = type(gen.getCollectionExpr());
					if (collType instanceof ListType) {
						OptionalInt s = ((ListType) collType).getSize();
						if (!s.isPresent()) {
							return new ListType(elementType, OptionalInt.empty());
						}
						for (VarDecl d : gen.getVariables()) {
							size *= s.getAsInt();
						}
					}
				}
				return new ListType(elementType, OptionalInt.of(size));
			}
		}

		default Type computeType(ExprVariable var) {
			return declaredType(names().declaration(var.getVariable()));
		}

		default Type computeType(ExprBinaryOp binary) {
			switch (binary.getOperations().get(0)) {
				case "&":
				case "|":
				case "^":
				case "/":
				case "div":
				case "**":
				case "-":
				case "%":
				case "mod":
				case "+":
				case "<<":
				case ">>":
				case "*":
					return leastUpperBound(type(binary.getOperands().get(0)), type(binary.getOperands().get(1)));
				case "&&":
				case "and":
				case "||":
				case "or":
				case ">=":
				case ">":
				case "<=":
				case "<":
				case "==":
				case "=":
				case "!=":
					return BoolType.INSTANCE;
				case "..":
					Type type = leastUpperBound(type(binary.getOperands().get(0)), type(binary.getOperands().get(1)));
					OptionalInt first = constants().intValue(binary.getOperands().get(0));
					OptionalInt last = constants().intValue(binary.getOperands().get(1));
					OptionalInt length;
					if (first.isPresent() && last.isPresent()) {
						length = OptionalInt.of(last.getAsInt() - first.getAsInt() + 1);
					} else {
						length = OptionalInt.empty();
					}
					return new RangeType(type, length);
				default:
					return BottomType.INSTANCE;
			}
		}

		default Type computeType(ExprUnaryOp unary) {
			Type t = type(unary.getOperand());
			switch (unary.getOperation()) {
				case "-": {
					if (t instanceof IntType) {
						IntType intType = (IntType) t;
						return new IntType(intType.getSize(), true);
					} else {
						return BottomType.INSTANCE;
					}
				}
				case "not": {
					if (t == BoolType.INSTANCE) {
						return t;
					} else {
						return BottomType.INSTANCE;
					}
				}
				default:
					return BottomType.INSTANCE;
			}
		}

		default Type computeType(ExprApplication apply) {
			Type function = type(apply.getFunction());
			if (function instanceof LambdaType) {
				return ((LambdaType) function).getReturnType();
			} else {
				return BottomType.INSTANCE;
			}
		}

		default Type computeType(ExprLambda lambda) {
			return new LambdaType(lambda.getValueParameters().map(this::declaredType), convert(lambda.getReturnType()));
		}

		default Type computeType(ExprProc lambda) {
			return new ProcType(lambda.getValueParameters().map(this::declaredType));
		}

		default Type computeType(ExprIndexer indexer) {
			Type structureType = type(indexer.getStructure());
			if (structureType instanceof ListType) {
				return ((ListType) structureType).getElementType();
			} else {
				return BottomType.INSTANCE;
			}
		}

		default Type computeType(ExprIf ifExpr) {
			Type thenType = type(ifExpr.getThenExpr());
			Type elseType = type(ifExpr.getElseExpr());
			return leastUpperBound(thenType, elseType);
		}

		default Type computeType(ExprInput input) {
			if (input.hasRepeat()) {
				// TODO fix this hack
				return portTypeRepeated(input.getPort(), new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(input.getRepeat())));
			} else {
				return portType(input.getPort());
			}
		}


		default Type leastUpperBound(Type a, Type b) {
			return TopType.INSTANCE;
		}

		default Type leastUpperBound(BottomType a, BottomType b) { return BoolType.INSTANCE; }
		default Type leastUpperBound(BottomType a, Type b) { return b; }
		default Type leastUpperBound(Type a, BottomType b) { return a; }

		default Type leastUpperBound(BoolType a, BoolType b) {
			return BoolType.INSTANCE;
		}

		default Type leastUpperBound(ListType a, ListType b) {
			Type elementLub = leastUpperBound(a.getElementType(), b.getElementType());
			OptionalInt size = a.getSize().equals(b.getSize()) ? a.getSize() : OptionalInt.empty();
			return new ListType(elementLub, size);
		}

		default Type leastUpperBound(RefType a, RefType b) {
			return new RefType(leastUpperBound(a, b));
		}

		default Type leastUpperBound(LambdaType a, LambdaType b) {
			if (a.getParameterTypes().size() != b.getParameterTypes().size()) {
				return TopType.INSTANCE;
			}
			List<Type> parTypes = new ArrayList<>();
			for (int i = 0; i < a.getParameterTypes().size(); i++) {
				Type glb = greatestLowerBound(a.getParameterTypes().get(i), b.getParameterTypes().get(i));
				if (glb == BottomType.INSTANCE) {
					return TopType.INSTANCE;
				}
				parTypes.add(glb);
			}
			Type returnType = leastUpperBound(a.getReturnType(), b.getReturnType());
			return new LambdaType(parTypes, returnType);
		}

		default Type leastUpperBound(IntType a, IntType b) {
			if (a.getSize().isPresent() && b.getSize().isPresent()) {
				int posBits = Math.max(positiveBits(a), positiveBits(b));
				int negBits = Math.max(negativeBits(a), negativeBits(b));
				if (negBits > 0) {
					int bits = Math.max(posBits, negBits) + 1;
					return new IntType(OptionalInt.of(bits), true);
				} else {
					int bits = posBits;
					return new IntType(OptionalInt.of(bits), false);
				}
			} else {
				return new IntType(OptionalInt.empty(), a.isSigned() || b.isSigned());
			}
		}

		default int positiveBits(IntType t) {
			if (t.isSigned()) {
				return t.getSize().getAsInt() - 1;
			} else {
				return t.getSize().getAsInt();
			}
		}
		default int negativeBits(IntType t) {
			if (t.isSigned()) {
				return t.getSize().getAsInt() - 1;
			} else {
				return 0;
			}
		}

		default Type greatestLowerBound(Type a, Type b) {
			return BottomType.INSTANCE;
		}

		default Type greatestLowerBound(BoolType a, BoolType b) {
			return BoolType.INSTANCE;
		}

		default Type greatestLowerBound(IntType a, IntType b) {
			if (a.getSize().isPresent() || b.getSize().isPresent()) {
				int size = Math.min(a.getSize().orElse(Integer.MAX_VALUE), b.getSize().orElse(Integer.MAX_VALUE));
				return new IntType(OptionalInt.of(size), a.isSigned() && b.isSigned());
			} else {
				return new IntType(OptionalInt.empty(), a.isSigned() && b.isSigned());
			}
		}

		default Type greatestLowerBound(ListType a, ListType b) {
			if (a.getSize().equals(b.getSize())) {
				return new ListType(greatestLowerBound(a.getElementType(), b.getElementType()), a.getSize());
			} else {
				return BottomType.INSTANCE;
			}
		}
	}
}
