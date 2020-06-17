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
public interface Maps {

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
				.bind("union").to(union())
				.bind("membership").to(membership())
				.bind("domain").to(domain())
				.bind("range").to(range())
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
				.bind("union").to(union())
				.bind("membership").to(membership())
				.bind("domain").to(domain())
				.bind("range").to(range())
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
	default Domain domain() {
		return MultiJ.from(Domain.class)
				.bind("backend").to(backend())
				.bind("code").to(backend().code())
				.bind("alias").to(backend().alias())
				.bind("emitter").to(backend().emitter())
				.bind("utils").to(utils())
				.instance();
	}

	@Binding(LAZY)
	default Range range() {
		return MultiJ.from(Range.class)
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

	default void forwardMap() {
		backend().emitter().emit("// FORWARD MAP DECLARATIONS");
		utils().types().forEach(type -> {
			forward().apply(type);
		});
	}

	default void declareMap() {
		backend().emitter().emit("// MAP DECLARATIONS");
		utils().types().forEach(type -> {
			prototypes().apply(type);
		});
	}

	default void defineMap() {
		backend().emitter().emit("// MAP DEFINITIONS");
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

		default void apply(MapType type) {
			emitter().emit("typedef %1$s* %2$s;", utils().internalName(type), utils().name(type));
			emitter().emit("typedef struct %1$s_t %1$s;", utils().entry(type));
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
		Union union();
		@Binding(BindingKind.INJECTED)
		Membership membership();
		@Binding(BindingKind.INJECTED)
		Domain domain();
		@Binding(BindingKind.INJECTED)
		Range range();

		default void apply(MapType type) {
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
			union().prototype(type);
			membership().prototype(type);
			domain().prototype(type);
			range().prototype(type);
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
		Union union();
		@Binding(BindingKind.INJECTED)
		Membership membership();
		@Binding(BindingKind.INJECTED)
		Domain domain();
		@Binding(BindingKind.INJECTED)
		Range range();

		default void apply(MapType type) {
			init().definition(type);
			free().definition(type);
			write().definition(type);
			read().definition(type);
			size().definition(type);
			resize().definition(type);
			copy().definition(type);
			compare().definition(type);
			add().definition(type);
			union().definition(type);
			membership().definition(type);
			domain().definition(type);
			range().definition(type);
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

		default void apply(MapType type) {
			emitter().emit("%s {", utils().internalName(type));
			emitter().increaseIndentation();
			emitter().emit("size_t capacity;");
			emitter().emit("size_t size;");
			emitter().emit("%s* data;", utils().entry(type));
			emitter().decreaseIndentation();
			emitter().emit("};");
			emitter().emit("");
			emitter().emit("struct %s_t {", utils().entry(type));
			emitter().increaseIndentation();
			emitter().emit("%s key;", code().type(type.getKeyType()));
			emitter().emit("%s value;", code().type(type.getValueType()));
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

		default void prototype(MapType type) {
			emitter().emit("%1$s init_%1$s(void);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("%1$s init_%1$s(void) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("%1$s self = calloc(1, sizeof(%2$s));", utils().name(type), utils().internalName(type));
			emitter().emit("if (self == NULL) return NULL;");
			emitter().emit("self->data = calloc(%1$s, sizeof(%2$s));", CAPACITY, utils().entry(type));
			emitter().emit("if (self->data == NULL) { free(self); return NULL; }");
			emitter().emit("self->capacity = %s;", CAPACITY);
			emitter().emit("self->size = 0;");
			emitter().emit("return self;");
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

		default void prototype(MapType type) {
			emitter().emit("void free_%1$s(%1$s self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("void free_%1$s(%1$s self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			backend().free().apply(type.getKeyType(), "self->data[i].key");
			backend().free().apply(type.getValueType(), "self->data[i].value");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("free(self->data);");
			emitter().emit("self->data = NULL;");
			emitter().emit("self->capacity = 0;");
			emitter().emit("self->size = 0;");
			emitter().emit("free(self);");
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

		default void prototype(MapType type) {
			emitter().emit("void write_%1$s(const %1$s self, char* buffer);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("void write_%1$s(const %1$s self, char* buffer) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL || buffer == NULL) return;");
			emitter().emit("char* ptr = buffer;");
			emitter().emit("*(size_t*) ptr = self->size;");
			emitter().emit("ptr = (char*)((size_t*) ptr + 1);");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			serialization().write(type.getKeyType(), "self->data[i].key", "ptr");
			serialization().write(type.getValueType(), "self->data[i].value", "ptr");
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

		default void prototype(MapType type) {
			emitter().emit("%s read_%1$s(char* buffer);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("%s read_%1$s(char* buffer) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (buffer == NULL) return NULL;");
			emitter().emit("if (buffer == NULL) return NULL;");
			emitter().emit("%s result = calloc(1, sizeof(%s));", utils().name(type), utils().internalName(type));
			emitter().emit("if (result == NULL) return NULL;");
			emitter().emit("char* ptr = buffer;");
			emitter().emit("result->size = *(size_t*) ptr;");
			emitter().emit("ptr = (char*)((size_t*) ptr + 1);");
			emitter().emit("result->capacity = result->size + (result->size %% %s);", CAPACITY);
			emitter().emit("result->data = result->size == 0 ? NULL : calloc(result->capacity, sizeof(%s));", utils().entry(type));
			emitter().emit("for (size_t i = 0; i < result->size; i++) {");
			emitter().increaseIndentation();
			serialization().read(type.getKeyType(), "result->data[i].key", "ptr");
			serialization().read(type.getValueType(), "result->data[i].value", "ptr");
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

		default void prototype(MapType type) {
			emitter().emit("size_t size_%1$s(const %1$s self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("size_t size_%1$s(const %1$s self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return 0;");
			emitter().emit("size_t size = 0;");
			emitter().emit("size += self->size;");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("size += %s;", sizeof().evaluate(type.getKeyType(), "self->data[i].key"));
			emitter().emit("size += %s;", sizeof().evaluate(type.getValueType(), "self->data[i].value"));
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

		default void prototype(MapType type) {
			emitter().emit("void resize_%1$s(%1$s self);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("void resize_%1$s(%1$s self) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("if (self->size < self->capacity) return;");
			emitter().emit("self->data = realloc(self->data, sizeof(%1$s) * (self->capacity + %2$s));", utils().entry(type), CAPACITY);
			emitter().emit("memset(self->data + self->capacity, 0, sizeof(%1$s) * %2$s);", utils().entry(type), CAPACITY);
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

		default void prototype(MapType type) {
			emitter().emit("void copy_%1$s(%1$s* lhs, const %1$s rhs);", utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("void copy_%1$s(%1$s* lhs, const %1$s rhs) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (lhs == NULL || rhs == NULL) return;");
			emitter().emit("if (*lhs == rhs) return;");
			emitter().emit("if (*lhs) { free_%s(*lhs); *lhs = NULL; }", utils().name(type));
			emitter().emit("if (!(*lhs)) *lhs = calloc(1, sizeof(%s));", utils().internalName(type));
			emitter().emit("for (size_t i = 0; i < rhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%s(*lhs, rhs->data[i].key, rhs->data[i].value);", utils().name(type));
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

		default void prototype(MapType type) {
			emitter().emit("%1$s compare_%2$s(const %2$s lhs, const %2$s rhs);", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("%1$s compare_%2$s(const %2$s lhs, const %2$s rhs) {", code().type(BoolType.INSTANCE), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (lhs == NULL && rhs == NULL) return true;");
			emitter().emit("if (lhs == NULL || rhs == NULL) return false;");
			emitter().emit("if (lhs->size != rhs->size) return false;");
			emitter().emit("size_t count = 0;");
			emitter().emit("for (size_t i = 0; i < lhs->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t j = 0; (j < rhs->size) && !(found); j++) {");
			emitter().increaseIndentation();
			emitter().emit("if (%1$s && %2$s) {",
			code().compare(type.getKeyType(), "lhs->data[i].key", type.getKeyType(), "rhs->data[j].key"),
			code().compare(type.getValueType(), "lhs->data[i].value", type.getValueType(), "rhs->data[j].value"));
			emitter().increaseIndentation();
			emitter().emit("count++;");
			emitter().emit("found = true;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return (count == lhs->size) && (count == rhs->size);");
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

		default void prototype(MapType type) {
			emitter().emit("void add_%1$s(%1$s self, %2$s key, %3$s value);", utils().name(type), code().type(type.getKeyType()), code().type(type.getValueType()));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("void add_%1$s(%1$s self, %2$s key, %3$s value) {", utils().name(type), code().type(type.getKeyType()), code().type(type.getValueType()));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return;");
			emitter().emit("if (membership_%s(self, key)) {", utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("size_t index;");
			emitter().emit("for (index = 0; index < self->size; index++) {");
			emitter().increaseIndentation();
			emitter().emit("if (%s) break;", code().compare(type.getKeyType(), "self->data[index].key", type.getKeyType(), "key"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			code().copy(type.getValueType(), "self->data[index].value", type.getValueType(), "value");
			emitter().decreaseIndentation();
			emitter().emit("} else {");
			emitter().increaseIndentation();
			emitter().emit("resize_%s(self);", utils().name(type));
			code().copy(type.getKeyType(), "self->data[self->size].key", type.getKeyType(), "key");
			code().copy(type.getValueType(), "self->data[self->size].value", type.getValueType(), "value");
			emitter().emit("self->size++;");
			emitter().decreaseIndentation();
			emitter().emit("}");
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

		default void prototype(MapType type) {
			// TODO need dynamic list since the value may be either a list of size 1 or a list of size 2
		}

		default void definition(MapType type) {
			// TODO need dynamic list since the value may be either a list of size 1 or a list of size 2
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

		default void prototype(MapType type) {
			emitter().emit("%1$s membership_%2$s(const %2$s self, %3$s elem);", code().type(BoolType.INSTANCE), utils().name(type), code().type(type.getKeyType()));
			emitter().emit("");
		}

		default void definition(MapType type) {
			emitter().emit("%1$s membership_%2$s(const %2$s self, %3$s elem) {", code().type(BoolType.INSTANCE), utils().name(type), code().type(type.getKeyType()));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return false;");
			emitter().emit("%s found = false;", code().type(BoolType.INSTANCE));
			emitter().emit("for (size_t i = 0; (i < self->size) && !(found); i++) {");
			emitter().increaseIndentation();
			emitter().emit("found |= %s;", code().compare(type.getKeyType(), "self->data[i].key", type.getKeyType(), "elem"));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return found;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Domain {

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

		default void prototype(MapType type) {
			emitter().emit("%1$s domain_%2$s(const %2$s self);", code().type(new SetType(type.getKeyType())), utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			SetType resultType = new SetType(type.getKeyType());
			emitter().emit("%1$s domain_%2$s(const %2$s self) {", code().type(resultType), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return NULL;");
			emitter().emit("%s result = init_%1$s();", code().type(resultType), backend().defaultValues().defaultValue(resultType));
			emitter().emit("if (result == NULL) return NULL;");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%1$s(result, self->data[i].key);", code().type(resultType));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Range {

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

		default void prototype(MapType type) {
			emitter().emit("%1$s range_%2$s(const %2$s self);", code().type(new SetType(type.getValueType())), utils().name(type));
			emitter().emit("");
		}

		default void definition(MapType type) {
			SetType resultType = new SetType(type.getValueType());
			emitter().emit("%1$s range_%2$s(const %2$s self) {", code().type(resultType), utils().name(type));
			emitter().increaseIndentation();
			emitter().emit("if (self == NULL) return NULL;");
			emitter().emit("%s result = init_%1$s();", code().type(resultType), backend().defaultValues().defaultValue(resultType));
			emitter().emit("if (result == NULL) return NULL;");
			emitter().emit("for (size_t i = 0; i < self->size; i++) {");
			emitter().increaseIndentation();
			emitter().emit("add_%1$s(result, self->data[i].value);", code().type(resultType));
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("return result;");
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		}
	}

	@Module
	interface Utils {

		@Binding(BindingKind.INJECTED)
		Backend backend();

		default String name(MapType type) {
			return backend().code().type(type);
		}

		default String internalName(MapType type) {
			return "struct " + backend().code().type(type) + "_t";
		}

		default String entry(MapType type) {
			return name(type) + "_entry_t";
		}

		default Stream<MapType> types() {
			return backend().task().walk()
					.flatMap(this::type)
					.distinct();
		}

		default Stream<MapType> type(IRNode node) {
			return Stream.empty();
		}

		default Stream<MapType> type(VarDecl decl) {
			return wrapIfMap(backend().types().declaredType(decl));
		}
		default Stream<MapType> type(Expression expr) {
			Type t = backend().types().type(expr);
			return wrapIfMap(t);
		}

		default Stream<MapType> wrapIfMap(Type t) {
			return Stream.empty();
		}

		default Stream<MapType> wrapIfMap(MapType t) {
			return Stream.of(t);
		}
	}
}
