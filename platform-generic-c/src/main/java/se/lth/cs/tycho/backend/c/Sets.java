package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.MapType;
import se.lth.cs.tycho.type.SetType;
import se.lth.cs.tycho.type.Type;

import java.util.stream.Stream;

import static org.multij.BindingKind.LAZY;

@Module
public interface Sets {

	@Binding(BindingKind.INJECTED)
	Backend backend();

	int CAPACITY = 10;

	@Binding(LAZY)
	default Forward forward() {
		return MultiJ.from(Forward.class)
				.bind("backend").to(backend())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Prototypes prototypes() {
		return MultiJ.from(Prototypes.class)
				.bind("typedef").to(typedef())
				.bind("init").to(init())
				.bind("free").to(free())
				.bind("write").to(write())
				.bind("read").to(read())
				.bind("size").to(size())
				.bind("resize").to(resize())
				.bind("copy").to(copy())
				.bind("compare").to(compare())
				.bind("add").to(add())
				.bind("intersect").to(intersect())
				.bind("union").to(union())
				.bind("difference").to(difference())
				.bind("membership").to(membership())
				.bind("lessThan").to(lessThan())
				.bind("lessThanEqual").to(lessThanEqual())
				.bind("greaterThan").to(greaterThan())
				.bind("greaterThanEqual").to(greaterThanEqual())
				.instance();
	}

	@Binding(LAZY)
	default Definitions definitions() {
		return MultiJ.from(Definitions.class)
				.bind("init").to(init())
				.bind("free").to(free())
				.bind("write").to(write())
				.bind("read").to(read())
				.bind("size").to(size())
				.bind("resize").to(resize())
				.bind("copy").to(copy())
				.bind("compare").to(compare())
				.bind("add").to(add())
				.bind("intersect").to(intersect())
				.bind("union").to(union())
				.bind("difference").to(difference())
				.bind("membership").to(membership())
				.bind("lessThan").to(lessThan())
				.bind("lessThanEqual").to(lessThanEqual())
				.bind("greaterThan").to(greaterThan())
				.bind("greaterThanEqual").to(greaterThanEqual())
				.instance();
	}

	@Binding(LAZY)
	default TypeDef typedef() {
		return MultiJ.from(TypeDef.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Init init() {
		return MultiJ.from(Init.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Free free() {
		return MultiJ.from(Free.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Write write() {
		return MultiJ.from(Write.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.bind("serialization").to(backend().serialization())
				.instance();
	}

	@Binding(LAZY)
	default Read read() {
		return MultiJ.from(Read.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.bind("serialization").to(backend().serialization())
				.instance();
	}

	@Binding(LAZY)
	default Size size() {
		return MultiJ.from(Size.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.bind("sizeof").to(backend().sizeof())
				.instance();
	}

	@Binding(LAZY)
	default Resize resize() {
		return MultiJ.from(Resize.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Copy copy() {
		return MultiJ.from(Copy.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Compare compare() {
		return MultiJ.from(Compare.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Add add() {
		return MultiJ.from(Add.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Intersect intersect() {
		return MultiJ.from(Intersect.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Union union() {
		return MultiJ.from(Union.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Difference difference() {
		return MultiJ.from(Difference.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Membership membership() {
		return MultiJ.from(Membership.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default LessThan lessThan() {
		return MultiJ.from(LessThan.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default LessThanEqual lessThanEqual() {
		return MultiJ.from(LessThanEqual.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default GreaterThan greaterThan() {
		return MultiJ.from(GreaterThan.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default GreaterThanEqual greaterThanEqual() {
		return MultiJ.from(GreaterThanEqual.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Utils utils() {
		return MultiJ.from(Utils.class)
				.bind("backend").to(backend())
				.instance();
	}

	default void forwardSet() {
		backend().emitter().emit("// FORWARD SET DECLARATIONS");
		utils().types().forEach(type -> {
			forward().apply(type);
		});
	}

	default void declareSet() {
		backend().emitter().emit("// SET DECLARATIONS");
		utils().types().forEach(type -> {
			prototypes().apply(type);
		});
	}

	default void defineSet() {
		backend().emitter().emit("// SET DEFINITIONS");
		utils().types().forEach(type -> {
			definitions().apply(type);
		});
	}

	@Module
	interface Forward {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void apply(SetType type) {
			emitter().emit("typedef struct %1$s_t %1$s;", utils().name(type));
			emitter().emit("");
		}
	}

	@Module
	interface Prototypes {

		@Binding(BindingKind.INJECTED)
		TypeDef typedef();
		@Binding(BindingKind.INJECTED)
		Init init();
		@Binding(BindingKind.INJECTED)
		Free free();
		@Binding(BindingKind.INJECTED)
		Write write();
		@Binding(BindingKind.INJECTED)
		Read read();
		@Binding(BindingKind.INJECTED)
		Size size();
		@Binding(BindingKind.INJECTED)
		Resize resize();
		@Binding(BindingKind.INJECTED)
		Copy copy();
		@Binding(BindingKind.INJECTED)
		Compare compare();
		@Binding(BindingKind.INJECTED)
		Add add();
		@Binding(BindingKind.INJECTED)
		Intersect intersect();
		@Binding(BindingKind.INJECTED)
		Union union();
		@Binding(BindingKind.INJECTED)
		Difference difference();
		@Binding(BindingKind.INJECTED)
		Membership membership();
		@Binding(BindingKind.INJECTED)
		LessThan lessThan();
		@Binding(BindingKind.INJECTED)
		LessThanEqual lessThanEqual();
		@Binding(BindingKind.INJECTED)
		GreaterThan greaterThan();
		@Binding(BindingKind.INJECTED)
		GreaterThanEqual greaterThanEqual();

		default void apply(SetType type) {
			typedef().apply(type);
			init().prototype(type);
			free().prototype(type);
			write().prototype(type);
			read().prototype(type);
			size().prototype(type);
			resize().prototype(type);
			copy().prototype(type);
			compare().prototype(type);
			add().prototype(type);
			intersect().prototype(type);
			union().prototype(type);
			difference().prototype(type);
			membership().prototype(type);
			lessThan().prototype(type);
			lessThanEqual().prototype(type);
			greaterThan().prototype(type);
			greaterThanEqual().prototype(type);
		}
	}

	@Module
	interface Definitions {

		@Binding(BindingKind.INJECTED)
		Init init();
		@Binding(BindingKind.INJECTED)
		Free free();
		@Binding(BindingKind.INJECTED)
		Write write();
		@Binding(BindingKind.INJECTED)
		Read read();
		@Binding(BindingKind.INJECTED)
		Size size();
		@Binding(BindingKind.INJECTED)
		Resize resize();
		@Binding(BindingKind.INJECTED)
		Copy copy();
		@Binding(BindingKind.INJECTED)
		Compare compare();
		@Binding(BindingKind.INJECTED)
		Add add();
		@Binding(BindingKind.INJECTED)
		Intersect intersect();
		@Binding(BindingKind.INJECTED)
		Union union();
		@Binding(BindingKind.INJECTED)
		Difference difference();
		@Binding(BindingKind.INJECTED)
		Membership membership();
		@Binding(BindingKind.INJECTED)
		LessThan lessThan();
		@Binding(BindingKind.INJECTED)
		LessThanEqual lessThanEqual();
		@Binding(BindingKind.INJECTED)
		GreaterThan greaterThan();
		@Binding(BindingKind.INJECTED)
		GreaterThanEqual greaterThanEqual();

		default void apply(SetType type) {
			init().definition(type);
			free().definition(type);
			write().definition(type);
			read().definition(type);
			size().definition(type);
			resize().definition(type);
			copy().definition(type);
			compare().definition(type);
			add().definition(type);
			intersect().definition(type);
			union().definition(type);
			difference().definition(type);
			membership().definition(type);
			lessThan().definition(type);
			lessThanEqual().definition(type);
			greaterThan().definition(type);
			greaterThanEqual().definition(type);
		}
	}

	@Module
	interface TypeDef {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void apply(SetType type) {
			emitter().emit("struct %s_t {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("size_t capacity;");
			emitter().emit("size_t size;");
			emitter().emit("%s* data;", code().type(type.getElementType()));
			emitter().decreaseIndentation();
			emitter().emit("};");
			emitter().emit("");
		}
	}

	@Module
	interface Init {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("void init_%1$s(%1$s* self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("void init_%1$s(%1$s* self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("self->capacity = %s;", CAPACITY);
			emitter().emit("self->size = 0;");
			emitter().emit("self->data = calloc(%1$s, sizeof(%2$s));", CAPACITY, code().type(type.getElementType()));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Free {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Utils utils();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();

		default void prototype(SetType type) {
			emitter().emit("void free_%1$s(%1$s* self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("void free_%1$s(%1$s* self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("free(self->data);");
			emitter().emit("self->data = NULL;");
			emitter().emit("self->capacity = 0;");
			emitter().emit("self->size = 0;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Write {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();
		@Binding(BindingKind.INJECTED)
		Serialization serialization();

		default void prototype(SetType type) {
			emitter().emit("void write_%1$s(const %1$s* self, char* buffer);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("void write_%1$s(const %1$s* self, char* buffer) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL || buffer == NULL) return;");
			emitter().emit("char* ptr = buffer;");
			emitter().emit("*(size_t*) ptr = self->size;");
			emitter().emit("ptr = (char*)((size_t*) ptr + 1);");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			serialization().write(type.getElementType(), "self->data[i]", "ptr");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Read {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();
		@Binding(BindingKind.INJECTED)
		Serialization serialization();

		default void prototype(SetType type) {
			emitter().emit("%s read_%1$s(char* buffer);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%s read_%1$s(char* buffer) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("%s result = %s;", utils().name(type), backend().defaultValues().defaultValue(type));
			emitter().emit("if (buffer == NULL) return result;");
			emitter().emit("char* ptr = buffer;");
			emitter().emit("result.size = *(size_t*) ptr;");
			emitter().emit("ptr = (char*)((size_t*) ptr + 1);");
			emitter().emit("result.capacity = result.size + (result.size %% %s);", CAPACITY);
			emitter().emit("result.data = result.size == 0 ? NULL : calloc(result.capacity, sizeof(%s));", code().type(type.getElementType()));
			emitter().emit("for (size_t i = 0; i < result.size; i++) {");
			emitter().increaseIndentation();
			serialization().read(type.getElementType(), "result.data[i]", "ptr");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Size {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();
		@Binding(BindingKind.INJECTED)
		SizeOf sizeof();

		default void prototype(SetType type) {
			emitter().emit("size_t size_%1$s(const %1$s* self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("size_t size_%1$s(const %1$s* self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return 0;");
			emitter().emit("size_t size = 0;");
			emitter().emit("size += self->size;");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("size += %s;", sizeof().evaluate(type.getElementType(), "self->data[i]"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return size;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Resize {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("void resize_%1$s(%1$s* self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("void resize_%1$s(%1$s* self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("if (self->size < self->capacity) return;");
			emitter().emit("self->data = realloc(self->data, sizeof(%1$s) * (self->capacity + %2$s));", code().type(type.getElementType()), CAPACITY);
			emitter().emit("memset(self->data + self->capacity, 0, sizeof(%1$s) * %2$s);", code().type(type.getElementType()), CAPACITY);
			emitter().emit("self->capacity += %s;", CAPACITY);
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Copy {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Utils utils();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();

		default void prototype(SetType type) {
			emitter().emit("void copy_%1$s(%1$s* lhs, const %1$s* rhs);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			String tmp = backend().variables().generateTemp();
			emitter().emit("void copy_%1$s(%1$s* lhs, const %1$s* rhs) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (lhs == NULL || rhs == NULL) return;");
			emitter().emit("memset(lhs->data, 0, sizeof(%s) * lhs->size);", code().type(type.getElementType()));
			emitter().emit("lhs->size = 0;");
			emitter().emit("for (size_t i = 0; i < rhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(lhs, rhs->data[i]);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Compare {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s compare_%2$s(const %2$s* lhs, const %2$s* rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s compare_%2$s(const %2$s* lhs, const %2$s* rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (lhs == NULL && rhs == NULL) return true;");
			emitter().emit("if (lhs == NULL || rhs == NULL) return false;");
			emitter().emit("if (lhs->size != rhs->size) return false;");
			emitter().emit("for (size_t i = 0; i < lhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("for (size_t j = 0; j < rhs->size; j++) {");
			emitter().increaseIndentation();
			emitter().emit("if (!(%s)) return false;", code().compare(type.getElementType(), "lhs->data[i]", type.getElementType(), "rhs->data[j]"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return true;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Add {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("void add_%1$s(%1$s* self, %2$s elem);", utils().name(type), code().type(type.getElementType()));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("void add_%1$s(%1$s* self, %2$s elem) {", utils().name(type), code().type(type.getElementType()));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("if (membership_%s(self, elem)) return;", utils().name(type));
			emitter().emit("resize_%s(self);", utils().name(type));
			code().copy(type.getElementType(), "self->data[self->size++]", type.getElementType(), "elem");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Intersect {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s intersect_%1$s(const %1$s* lhs, const %1$s* rhs);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s intersect_%1$s(const %1$s* lhs, const %1$s* rhs) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("%1$s result = %2$s;", utils().name(type), backend().defaultValues().defaultValue(type));
			emitter().emit("init_%1$s(&result);", utils().name(type));
			emitter().emit("if (lhs == NULL || rhs == NULL) return result;");
			emitter().emit("for (size_t i = 0; i < lhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t j = 0; (j < rhs->size) && !(found); j++) {");
			emitter().increaseIndentation();
			emitter().emit("found |= %s;", code().compare(type.getElementType(), "lhs->data[i]", type.getElementType(), "rhs->data[j]"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("if (found) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(&result, lhs->data[i]);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Union {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s union_%1$s(const %1$s* lhs, const %1$s* rhs);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s union_%1$s(const %1$s* lhs, const %1$s* rhs) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("%1$s result = %2$s;", utils().name(type), backend().defaultValues().defaultValue(type));
			emitter().emit("init_%1$s(&result);", utils().name(type));
			emitter().emit("if (lhs == NULL || rhs == NULL) return result;");
			emitter().emit("for (size_t i = 0; i < lhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(&result, lhs->data[i]);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("for (size_t i = 0; i < rhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(&result, rhs->data[i]);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Difference {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s difference_%1$s(const %1$s* lhs, const %1$s* rhs);", utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s difference_%1$s(const %1$s* lhs, const %1$s* rhs) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("%1$s result = %2$s;", utils().name(type), backend().defaultValues().defaultValue(type));
			emitter().emit("init_%1$s(&result);", utils().name(type));
			emitter().emit("if (lhs == NULL || rhs == NULL) return result;");
			emitter().emit("for (size_t i = 0; i < lhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t j = 0; (j < rhs->size) && !(found); j++) {");
			emitter().increaseIndentation();
			emitter().emit("found |= %s;", code().compare(type.getElementType(), "lhs->data[i]", type.getElementType(), "rhs->data[j]"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("if (!(found)) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(&result, lhs->data[i]);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Membership {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s membership_%2$s(const %2$s* self, %3$s elem);", code().type(BoolType.INSTANCE), utils().name(type), code().type(type.getElementType()));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s membership_%2$s(const %2$s* self, %3$s elem) {", code().type(BoolType.INSTANCE), utils().name(type), code().type(type.getElementType()));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return false;");
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t i = 0; (i < self->size) && !(found); i++) {");
			emitter().increaseIndentation();
			emitter().emit("found |= %s;", code().compare(type.getElementType(), "self->data[i]", type.getElementType(), "elem"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return found;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface LessThan {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s less_than_%2$s(const %2$s* lhs, const %2$s* rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s less_than_%2$s(const %2$s* lhs, const %2$s* rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (lhs == NULL || rhs == NULL) return false;");
			emitter().emit("if (lhs->size >= rhs->size) return false;");
			emitter().emit("size_t count = 0;");
			emitter().emit("for (size_t i = 0; (i < lhs->size); i++) {");
			emitter().increaseIndentation();
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t j = 0; (j < rhs->size) && !(found); j++) {");
			emitter().increaseIndentation();
			emitter().emit("if (%s) {", code().compare(type.getElementType(), "lhs->data[i]", type.getElementType(), "rhs->data[j]"));
			emitter().increaseIndentation();
			emitter().emit("count++;");
			emitter().emit("found = true;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return (count == lhs->size) && (count < rhs->size);");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface LessThanEqual {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s less_than_equal_%2$s(const %2$s* lhs, const %2$s* rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s less_than_equal_%2$s(const %2$s* lhs, const %2$s* rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("return less_than_%1$s(lhs, rhs) || compare_%1$s(lhs, rhs);", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface GreaterThan {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s greater_than_%2$s(const %2$s* lhs, const %2$s* rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s greater_than_%2$s(const %2$s* lhs, const %2$s* rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("return !(less_than_equal_%1$s(lhs, rhs));", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface GreaterThanEqual {

		@Binding(BindingKind.INJECTED)
		Backend backend();
		@Binding(BindingKind.INJECTED)
		Code code();
		@Binding(BindingKind.INJECTED)
		Alias alias();
		@Binding(BindingKind.INJECTED)
		Emitter emitter();
		@Binding(BindingKind.INJECTED)
		Utils utils();

		default void prototype(SetType type) {
			emitter().emit("%1$s greater_than_equal_%2$s(const %2$s* lhs, const %2$s* rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(SetType type) {
			emitter().emit("%1$s greater_than_equal_%2$s(const %2$s* lhs, const %2$s* rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("return !(less_than_%1$s(lhs, rhs));", utils().name(type));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Utils {

		@Binding(BindingKind.INJECTED)
		Backend backend();

		default String name(SetType type) {
			return backend().code().type(type);
		}

		default Stream<SetType> types() {
			return backend().task().walk()
					.flatMap(this::type)
					.distinct();
		}

		default Stream<SetType> type(IRNode node) {
			return Stream.empty();
		}

		default Stream<SetType> type(VarDecl decl) {
			return wrapIfSet(backend().types().declaredType(decl));
		}
		default Stream<SetType> type(Expression expr) {
			Type t = backend().types().type(expr);
			return wrapIfSet(t);
		}

		default Stream<SetType> wrapIfSet(Type t) {
			return Stream.empty();
		}

		default Stream<SetType> wrapIfSet(SetType t) {
			return Stream.of(t);
		}

		default Stream<SetType> wrapIfSet(MapType t) {
			return Stream.of(new SetType(t.getValueType()), new SetType(t.getKeyType()));
		}
	}
}
