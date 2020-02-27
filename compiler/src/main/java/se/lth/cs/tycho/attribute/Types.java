package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.decl.*;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.stmt.lvalue.*;
import se.lth.cs.tycho.ir.type.FunctionTypeExpr;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.type.ProcedureTypeExpr;
import se.lth.cs.tycho.ir.type.TupleTypeExpr;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.type.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static se.lth.cs.tycho.util.CheckedCasts.toOptInt;

public interface Types {

	ModuleKey<Types> key = unit -> MultiJ.from(Implementation.class)
			.bind("ports").to(unit.getModule(Ports.key))
			.bind("variables").to(unit.getModule(VariableDeclarations.key))
			.bind("parameters").to(unit.getModule(ParameterDeclarations.key))
			.bind("constants").to(unit.getModule(ConstantEvaluator.key))
			.bind("tree").to(unit.getModule(TreeShadow.key))
			.bind("globalNames").to(unit.getModule(GlobalNames.key))
			.bind("typeScopes").to(unit.getModule(TypeScopes.key))
			.instance();

	Type declaredType(VarDecl decl);
	Type declaredGlobalType(GlobalTypeDecl decl);
	Type type(Expression expr);
	Type lvalueType(LValue lvalue);
	Type declaredPortType(PortDecl port);
	Type portType(Port port);
	Type portTypeRepeated(Port port, Expression repeat);
	Type connectionType(Network network, Connection conn);

	@Module
	interface Implementation extends Types {

		@Binding(BindingKind.INJECTED)
		VariableDeclarations variables();

		@Binding(BindingKind.INJECTED)
		ParameterDeclarations parameters();

		@Binding(BindingKind.INJECTED)
		Ports ports();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Binding(BindingKind.INJECTED)
		ConstantEvaluator constants();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Binding(BindingKind.LAZY)
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

		@Binding(BindingKind.LAZY)
		default ThreadLocal<Set<VarDecl>> currentlyComputing() {
			return ThreadLocal.withInitial(HashSet::new);
		}

		@Binding(BindingKind.LAZY)
		default Map<VarDecl, Type> declaredTypeMap() {
			return new ConcurrentHashMap<>();
		}

		@Binding(BindingKind.LAZY)
		default Map<GlobalTypeDecl, Type> declaredGlobalTypeMap() {
			return new ConcurrentHashMap<>();
		}

		default SourceUnit getSourceUnit(IRNode node) {
			do {
				if (node instanceof SourceUnit) {
					return (SourceUnit) node;
				}
				node = tree().parent(node);
			} while (node != null);
			return null;
		}

		default Type declaredType(VarDecl varDecl) {
			if (currentlyComputing().get().contains(varDecl)) {
				return new ErrorType(new Diagnostic(Diagnostic.Kind.ERROR, "Type of variable has circular dependency.", getSourceUnit(varDecl), varDecl));
			} else if (declaredTypeMap().containsKey(varDecl)) {
				return declaredTypeMap().get(varDecl);
			} else {
				currentlyComputing().get().add(varDecl);
				Type t = computeDeclaredType(varDecl);
				currentlyComputing().get().remove(varDecl);
				Type old = declaredTypeMap().putIfAbsent(varDecl, t);
				return old != null ? old : t;
			}
		}

		default Type declaredGlobalType(GlobalTypeDecl decl) {
			if (!declaredGlobalTypeMap().containsKey(decl)) {
				AlgebraicTypeDecl algebraicTypeDecl = decl.getDeclaration();
				if (algebraicTypeDecl instanceof ProductTypeDecl) {
					ProductTypeDecl productTypeDecl = (ProductTypeDecl) algebraicTypeDecl;
					declaredGlobalTypeMap().put(decl, new ProductType(productTypeDecl.getName(), productTypeDecl.getFields().stream().map(field -> new FieldType(field.getName(), convert(field.getType()))).collect(ImmutableList.collector())));
				} else if (algebraicTypeDecl instanceof SumTypeDecl) {
					SumTypeDecl sumTypeDecl = (SumTypeDecl) algebraicTypeDecl;
					declaredGlobalTypeMap().put(decl, new SumType(sumTypeDecl.getName(), sumTypeDecl.getVariants().stream().map(variant -> new SumType.VariantType(variant.getName(), variant.getFields().stream().map(field -> new FieldType(field.getName(), convert(field.getType()))).collect(ImmutableList.collector()))).collect(ImmutableList.collector())));
				}
			}
			return declaredGlobalTypeMap().get(decl);
		}

		default Type computeDeclaredType(VarDecl varDecl) {
			if (varDecl.getType() != null) {
				return convert(varDecl.getType());
			} else if (varDecl.getValue() != null) {
				return type(varDecl.getValue());
			} else {
				return new ErrorType(new Diagnostic(Diagnostic.Kind.ERROR, "Variable declaration requires initial value or type.", getSourceUnit(varDecl), varDecl));
			}
		}

		default Type computeDeclaredType(InputVarDecl varDecl) {
			InputPattern input = (InputPattern) tree().parent(varDecl);
			PortDecl port = ports().declaration(input.getPort());
			Type result = convert(port.getType());
			if (input.getRepeatExpr() != null) {
				OptionalLong size = constants().intValue(input.getRepeatExpr());
				return new ListType(result, toOptInt(size));
			} else {
				return result;
			}
		}

		default Type computeDeclaredType(GeneratorVarDecl varDecl) {
			Generator generator = (Generator) tree().parent(varDecl);
			if (generator.getType() != null) {
				return convert(generator.getType());
			} else {
				return elementType(type(generator.getCollection()))
						.orElse(BottomType.INSTANCE);
			}
		}

		default Optional<Type> elementType(Type type) {
			return Optional.empty();
		}

		default Optional<Type> elementType(RangeType type) {
			return Optional.of(type.getType());
		}

		default Optional<Type> elementType(ListType type) {
			return Optional.of(type.getElementType());
		}

		@Binding(BindingKind.LAZY)
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
			return declaredPortType(ports().declaration(port));
		}

		default Type portTypeRepeated(Port port, Expression repeat) {
			Type element = portType(port);
			OptionalLong size = constants().intValue(repeat);
			return new ListType(element, toOptInt(size));
		}

		default Type connectionType(Network network, Connection conn) {
			Type tokenType;
			if (conn.getSource().getInstance().isPresent()) {
				Instance instance = network.getInstances().stream()
						.filter(inst -> inst.getInstanceName().equals(conn.getSource().getInstance().get()))
						.findFirst().get();
				GlobalEntityDecl entity = globalNames().entityDecl(instance.getEntityName(), true);
				PortDecl portDecl = entity.getEntity().getOutputPorts().stream()
						.filter(port -> port.getName().equals(conn.getSource().getPort()))
						.findFirst().orElseThrow(() -> new AssertionError("Missing source port: " + conn));
				tokenType = declaredPortType(portDecl);
			} else {
				PortDecl portDecl = network.getInputPorts().stream()
						.filter(port -> port.getName().equals(conn.getSource().getPort()))
						.findFirst().get();
				tokenType = declaredPortType(portDecl);
			}
			return tokenType;
		}

		Type computeLValueType(LValue lvalue);

		default Type computeLValueType(LValueVariable var) {
			return declaredType(variables().declaration(var.getVariable()));
		}

		default Type computeLValueType(LValueDeref deref) {
			Type referenceType = computeLValueType(deref.getVariable());
			if (referenceType instanceof RefType) {
				return ((RefType) referenceType).getType();
			} else {
				return BottomType.INSTANCE;
			}
		}

		default Type computeLValueType(LValueIndexer indexer) {
			Type structureType = computeLValueType(indexer.getStructure());
			if (structureType instanceof ListType) {
				return ((ListType) structureType).getElementType();
			} else {
				return BottomType.INSTANCE;
			}
		}

		default Type computeLValueType(LValueField fieldLValue) {
			Type structureType = computeLValueType(fieldLValue.getStructure());
			if (structureType instanceof ProductType) {
				ProductType productType = (ProductType) structureType;
				return productType.getFields()
						.stream()
						.filter(field -> Objects.equals(field.getName(), fieldLValue.getField().getName()))
						.map(FieldType::getType)
						.findAny()
						.orElse(BottomType.INSTANCE);
			} else {
				return BottomType.INSTANCE;
			}
		}


		Type convert(TypeExpr t);

		default TupleType convert(TupleTypeExpr t) {
			return new TupleType(t.getTypes().map(this::convert));
		}

		default LambdaType convert(FunctionTypeExpr t) {
			return new LambdaType(t.getParameterTypes().map(this::convert), convert(t.getReturnType()));
		}

		default ProcType convert(ProcedureTypeExpr t) {
			return new ProcType(t.getParameterTypes().map(this::convert));
		}

		default Type convert(NominalTypeExpr t) {
			switch (t.getName()) {
				case "List": {
					Optional<TypeExpr> e = findParameter(t.getTypeParameters(), "type");
					Optional<Type> elements = e.map(this::convert);
					if (elements.isPresent()) {
						Optional<Expression> s = findParameter(t.getValueParameters(), "size");
						if (s.isPresent()) {
							OptionalLong size = constants().intValue(s.get());
							if (size.isPresent()) {
								return new ListType(elements.get(), toOptInt(size));
							}
						}
						return new ListType(elements.get(), OptionalInt.empty());
					}
					return BottomType.INSTANCE;
				}
				case "Queue": {
					Optional<TypeExpr> e = findParameter(t.getTypeParameters(), "token");
					Optional<Type> elements = e.map(this::convert);
					if (elements.isPresent()) {
						Optional<Expression> s = findParameter(t.getValueParameters(), "size");
						if (s.isPresent()) {
							OptionalLong size = constants().intValue(s.get());
							if (size.isPresent()) {
								return new QueueType(elements.get(), (int) size.getAsLong());
							}
						}
					}
					return BottomType.INSTANCE;
				}
				case "Ref": {
					return findParameter(t.getTypeParameters(), "type")
							.map(this::convert)
							.<Type> map(RefType::new)
							.orElse(BottomType.INSTANCE);
				}
				case "int": {
					Optional<Expression> s = findParameter(t.getValueParameters(), "size");
					if (s.isPresent()) {
						OptionalLong size = constants().intValue(s.get());
						if (size.isPresent()) {
							return new IntType(toOptInt(size), true);
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
						OptionalLong size = constants().intValue(s.get());
						if (size.isPresent()) {
							return new IntType(toOptInt(size), false);
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
				case "float": {
					return RealType.f32;
				}
				case "double": {
					return RealType.f64;
				}
				case "String": {
					return StringType.INSTANCE;
				}
				default:
					Optional<TypeDecl> decl = typeScopes().declaration(t);
					if (decl.isPresent()) {
						return declaredGlobalType((GlobalTypeDecl) decl.get());
					}
					return BottomType.INSTANCE;
			}
		}

		default <E extends IRNode, P extends Parameter<E, P>> Optional<E> findParameter(List<P> parameters, String name) {
			return parameters.stream()
					.filter(param -> param.getName().equals(name))
					.map(Parameter::getValue)
					.findFirst();
		}

		Type computeType(Expression e);

		default Type computeType(ExprDeref e) {
			Type referenceType = type(e.getReference());
			if (referenceType instanceof RefType) {
				return ((RefType) referenceType).getType();
			} else {
				return BottomType.INSTANCE;
			}
		}

		default Type computeType(ExprLiteral e) {
			switch (e.getKind()) {
				case True:
				case False: {
					return BoolType.INSTANCE;
				}
				case Integer: {
					OptionalLong value = constants().intValue(e);
					final long v = value.getAsLong();
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
				case Real: {
					return RealType.f32;
				}
				default: {
					return BottomType.INSTANCE;
				}
			}
		}

		default OptionalInt collectionSize(Type t) {
			return OptionalInt.empty();
		}

		default OptionalInt collectionSize(ListType t) {
			return t.getSize();
		}

		default OptionalInt collectionSize(RangeType t) {
			return t.getLength();
		}

		Type withCollectionSize(Type t, OptionalInt size);

		default Type withCollectionSize(ListType t, OptionalInt size) {
			return new ListType(t.getElementType(), size);
		}

		default Type computeType(ExprComprehension comprehension) {
			Type collectionType = type(comprehension.getCollection());
			OptionalInt collectionSize = collectionSize(collectionType);
			Type generatorType = type(comprehension.getGenerator().getCollection());
			OptionalInt generatorSize = collectionSize(generatorType);
			OptionalInt size;
			if (collectionSize.isPresent() && generatorSize.isPresent() && comprehension.getFilters().isEmpty()) {
				int s = collectionSize.getAsInt() *
						(int) Math.pow(generatorSize.getAsInt(), comprehension.getGenerator().getVarDecls().size());
				size = OptionalInt.of(s);
			} else {
				size = OptionalInt.empty();
			}
			return withCollectionSize(collectionType, size);
		}

		default Type computeType(ExprLet let) {
			return type(let.getBody());
		}

		default Type computeType(ExprList list) {
			Type elementType = list.getElements().stream()
					.map(this::type)
					.reduce(BottomType.INSTANCE, this::leastUpperBound);
			return new ListType(elementType, OptionalInt.of(list.getElements().size()));
		}

		default Type computeType(ExprRef ref) {
			return new RefType(declaredType(variables().declaration(ref.getVariable())));
		}

		default Type computeType(ExprVariable var) {
			return declaredType(variables().declaration(var.getVariable()));
		}

		default Type computeType(ExprGlobalVariable var) {
			return declaredType(globalNames().varDecl(var.getGlobalName(), true));
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
					OptionalLong first = constants().intValue(binary.getOperands().get(0));
					OptionalLong last = constants().intValue(binary.getOperands().get(1));
					OptionalLong length;
					if (first.isPresent() && last.isPresent()) {
						length = OptionalLong.of(last.getAsLong() - first.getAsLong() + 1);
					} else {
						length = OptionalLong.empty();
					}
					return new RangeType(type, toOptInt(length));
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
						if (unary.getOperand() instanceof ExprLiteral) {
							return new IntType(OptionalInt.of(intType.getSize().getAsInt() + 1), true);
						}
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

		default Type computeType(ExprTypeConstruction construction) {
			return declaredGlobalType((GlobalTypeDecl) typeScopes().declaration(construction).get());
		}

		default Type computeType(ExprField fieldExpr) {
			Type structureType = type(fieldExpr.getStructure());
			if (structureType instanceof ProductType) {
				ProductType productType = (ProductType) structureType;
				return productType.getFields()
						.stream()
						.filter(field -> Objects.equals(field.getName(), fieldExpr.getField().getName()))
						.map(FieldType::getType)
						.findAny()
						.orElse(BottomType.INSTANCE);
			} else {
				return BottomType.INSTANCE;
			}
		}


		default Type leastUpperBound(Type a, Type b) {
			return TopType.INSTANCE;
		}

		default Type leastUpperBound(BottomType a, BottomType b) { return BottomType.INSTANCE; }
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
					return new IntType(OptionalInt.of(posBits), false);
				}
			} else {
				return new IntType(OptionalInt.empty(), a.isSigned() || b.isSigned());
			}
		}

		default Type leastUpperBound(RealType a, RealType b) {
			return RealType.of(Math.max(a.getSize(), b.getSize()));
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
