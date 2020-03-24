package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
import se.lth.cs.tycho.type.BoolType;
import se.lth.cs.tycho.type.ProductType;
import se.lth.cs.tycho.type.SumType;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public interface AlgebraicTypes {
	@Binding(BindingKind.INJECTED)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Code code() {
		return backend().code();
	}

	default void forwardDeclareAlgebraicTypes() {
		emitter().emit("// FORWARD TYPE DECLARATIONS");
		types().forEachOrdered(this::forwardDeclareType);
	}

	default void forwardDeclareType(AlgebraicType type) {}

	default void forwardDeclareType(ProductType product) {
		emitter().emit("typedef struct %s_s %s_t;", product.getName(), product.getName());
		emitter().emit("");
	}

	default void forwardDeclareType(SumType sum) {
		emitter().emit("typedef struct %s_s %s_t;", sum.getName(), sum.getName());
		emitter().emit("");
	}

	default void declareAlgebraicTypes() {
		emitter().emit("// TYPE DECLARATIONS");
		types().forEachOrdered(this::declareType);
	}

	default void declareType(AlgebraicType type) {}

	default void declareType(ProductType product) {
		emitter().emit("struct %s_s {", product.getName());
		emitter().increaseIndentation();
		product.getFields().forEach(field -> {
			emitter().emit("%s;", code().declaration(field.getType(), field.getName()));
		});
		emitter().decreaseIndentation();
		emitter().emit("};");
		emitter().emit("");
		emitter().emit("%s* init_%s_t(%s);", code().type(product), product.getName(), product.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		emitter().emit("");
		emitter().emit("void free_%s_t(%s);", product.getName(), code().declaration(product, "self"));
		emitter().emit("");
		emitter().emit("void write_%s_t(%s, char *buffer);", product.getName(), code().declaration(product, "self"));
		emitter().emit("");
		emitter().emit("%s* read_%s_t(char *buffer);", code().type(product), product.getName());
		emitter().emit("");
		emitter().emit("size_t size_%s_t(%s);", product.getName(), code().declaration(product, "self"));
		emitter().emit("");
		emitter().emit("void copy_%s_t(%s, %s);", product.getName(), code().declaration(product, "*to"), code().declaration(product, "from"));
		emitter().emit("");
		emitter().emit("%s compare_%s_t(%s, %s);", code().type(BoolType.INSTANCE), product.getName(), code().declaration(product, "lhs"), code().declaration(product, "rhs"));
		emitter().emit("");
	}

	default void declareType(SumType sum) {
		emitter().emit("enum %s_tag_t {", sum.getName());
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("tag_%s_%s%s", sum.getName(), variant.getName(), sum.getVariants().indexOf(variant) == sum.getVariants().size() - 1 ? "" : ",");
		});
		emitter().decreaseIndentation();
		emitter().emit("};");
		emitter().emit("");
		emitter().emit("struct %s_s {", sum.getName());
		emitter().increaseIndentation();
		emitter().emit("enum %s_tag_t tag;", sum.getName());
		emitter().emit("union {");
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("struct {");
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				emitter().emit("%s;", code().declaration(field.getType(), field.getName()));
			});
			emitter().decreaseIndentation();
			emitter().emit("} %s;", variant.getName());
		});
		emitter().decreaseIndentation();
		emitter().emit("} data;");
		emitter().decreaseIndentation();
		emitter().emit("};");
		sum.getVariants().forEach(variant -> {
			emitter().emit("");
			emitter().emit("%s* init_%s_t_%s(%s);", code().type(sum), sum.getName(), variant.getName(), variant.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		});
		emitter().emit("");
		emitter().emit("void free_%s_t(%s);", sum.getName(), code().declaration(sum, "self"));
		emitter().emit("");
		emitter().emit("void write_%s_t(%s, char *buffer);", sum.getName(), code().declaration(sum, "self"));
		emitter().emit("");
		emitter().emit("%s* read_%s_t(char *buffer);", code().type(sum), sum.getName());
		emitter().emit("");
		emitter().emit("size_t size_%s_t(%s);", sum.getName(), code().declaration(sum, "self"));
		emitter().emit("");
		emitter().emit("void copy_%s_t(%s, %s);", sum.getName(), code().declaration(sum, "*to"), code().declaration(sum, "from"));
		emitter().emit("");
		emitter().emit("%s compare_%s_t(%s, %s);", BoolType.INSTANCE, sum.getName(), code().declaration(sum, "lhs"), code().declaration(sum, "rhs"));
		emitter().emit("");
	}

	default void defineAlgebraicTypes() {
		emitter().emit("// TYPE DEFINITIONS");
		types().forEachOrdered(this::defineType);
	}

	default void defineType(AlgebraicType type) {}

	default void defineType(ProductType product) {
		defineInit(product);
		defineFree(product);
		defineWrite(product);
		defineRead(product);
		defineSize(product);
		defineCopy(product);
		defineCompare(product);
	}

	default void defineInit(ProductType product) {
		String self = "self";
		emitter().emit("%s* init_%s_t(%s) {", code().type(product), product.getName(), product.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		emitter().increaseIndentation();
		emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(product, self), product.getName());
		emitter().emit("if (!%s) return NULL;", self);
		product.getFields().forEach(field ->  code().copy(field.getType(), String.format("%s->%s", self, field.getName()), field.getType(), field.getName()));
		emitter().emit("return %s;", self);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineFree(ProductType product) {
		String self = "self";
		emitter().emit("void free_%s_t(%s) {", product.getName(), code().declaration(product, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s) return;", self);
		product.getFields().forEach(field -> {
			if (field.getType() instanceof AlgebraicType) {
				emitter().emit("%s(%s->%s);", destructor((AlgebraicType) field.getType()), self, field.getName());
			}
		});
		emitter().emit("free(%s);", self);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineWrite(ProductType product) {
		String self = "self";
		String ptr = "ptr";
		emitter().emit("void write_%s_t(%s, char *buffer) {", product.getName(), code().declaration(product, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !buffer) return;", self);
		emitter().emit("char *%s = buffer;", ptr);
		product.getFields().forEach(field -> {
			if (field.getType() instanceof AlgebraicType) {
				emitter().emit("write_%s_t(%s->%s, %s);", ((AlgebraicType) field.getType()).getName(), self, field.getName(), ptr);
				emitter().emit("%s += size_%s_t(%s->%s);", ptr, ((AlgebraicType) field.getType()).getName(), self, field.getName());
			} else {
				emitter().emit("*(%s*) %s = %s->%s;", code().type(field.getType()), ptr, self, field.getName());
				emitter().emit("%s = (char*)((%s*) %s + 1);", ptr, code().type(field.getType()), ptr);
			}
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineRead(ProductType product) {
		String self = "self";
		String ptr = "ptr";
		emitter().emit("%s* read_%s_t(char *buffer) {", code().type(product), product.getName());
		emitter().increaseIndentation();
		emitter().emit("if (!buffer) return NULL;");
		emitter().emit("char *%s = buffer;", ptr);
		emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(product, self), product.getName());
		emitter().emit("if (!%s) return NULL;", self);
		product.getFields().forEach(field -> {
			if (field.getType() instanceof AlgebraicType) {
				emitter().emit("%s->%s = read_%s_t(%s);", self, field.getName(), ((AlgebraicType) field.getType()).getName(), ptr);
				emitter().emit("%s += size_%s_t(%s->%s);", ptr, ((AlgebraicType) field.getType()).getName(), self, field.getName());
			} else {
				emitter().emit("%s->%s = *(%s*) %s;", self, field.getName(), code().type(field.getType()), ptr);
				emitter().emit("%s = (char*)((%s*) %s + 1);", ptr, code().type(field.getType()), ptr);
			}
		});
		emitter().emit("return %s;", self);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineSize(ProductType product) {
		String self = "self";
		String size = "size";
		emitter().emit("size_t size_%s_t(%s) {", product.getName(), code().declaration(product, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s) return 0;", self);
		emitter().emit("size_t %s = 0;", size);
		product.getFields().forEach(field -> {
			if (field.getType() instanceof AlgebraicType) {
				emitter().emit("%s += size_%s_t(%s->%s);", size, ((AlgebraicType) field.getType()).getName(), self, field.getName());
			} else{
				emitter().emit("%s += sizeof(%s);", size, code().type(field.getType()));
			}
		});
		emitter().emit("return %s;", size);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineCopy(ProductType product) {
		String from = "from";
		String to = "to";
		emitter().emit("void copy_%s_t(%s, %s) {", product.getName(), code().declaration(product, "*" + to), code().declaration(product, from));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !%s) return;", to, from);
		emitter().emit("if (*%s) { %s(*%s); *%s = NULL; }", to, destructor(product), to, to);
		emitter().emit("if (!(*%s)) *%s = calloc(1, sizeof(%s_t));", to, to, product.getName());
		emitter().emit("if (!(*%s)) return;", to);
		product.getFields().forEach(field -> {
			code().copy(field.getType(), "(*" + to + ")->" + field.getName(), field.getType(), from + "->" + field.getName());
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineCompare(ProductType product) {
		String lhs = "lhs";
		String rhs = "rhs";
		emitter().emit("%s compare_%s_t(%s, %s) {", code().type(BoolType.INSTANCE), product.getName(), code().declaration(product, lhs), code().declaration(product, rhs));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !%s) return false;", lhs, rhs);
		product.getFields().forEach(field -> {
			emitter().emit("if (!%s) return false;", code().compare(field.getType(), String.format("%s->%s", lhs, field.getName()), field.getType(), String.format("%s->%s", rhs, field.getName())));
		});
		emitter().emit("return true;");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineType(SumType sum) {
		defineInit(sum);
		defineFree(sum);
		defineWrite(sum);
		defineRead(sum);
		defineSize(sum);
		defineCopy(sum);
		defineCompare(sum);
	}

	default void defineInit(SumType sum) {
		sum.getVariants().forEach(variant -> {
			String self = "self";
			emitter().emit("%s* init_%s_t_%s(%s) {", code().type(sum), sum.getName(), variant.getName(), variant.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
			emitter().increaseIndentation();
			emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(sum, self), sum.getName());
			emitter().emit("if (!%s) return NULL;", self);
			emitter().emit("%s->tag = tag_%s_%s;", self, sum.getName(), variant.getName());
			variant.getFields().forEach(field -> {
				code().copy(field.getType(), String.format("%s->data.%s.%s", self, variant.getName(), field.getName()), field.getType(), field.getName());
			});
			emitter().emit("return %s;", self);
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		});
	}

	default void defineFree(SumType sum) {
		String self = "self";
		emitter().emit("void free_%s_t(%s) {", sum.getName(), code().declaration(sum, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s) return;", self);
		emitter().emit("switch (%s->tag) {", self);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s:", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				if (field.getType() instanceof AlgebraicType) {
					emitter().emit("%s(%s->data.%s.%s);", destructor((AlgebraicType) field.getType()), self, variant.getName(), field.getName());
				}
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("free(%s);", self);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineWrite(SumType sum) {
		String self = "self";
		String ptr = "ptr";
		emitter().emit("void write_%s_t(%s, char *buffer) {", sum.getName(), code().declaration(sum, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !buffer) return;", self);
		emitter().emit("char *%s = buffer;", ptr);
		emitter().emit("*(enum %s_tag_t*) %s = %s->tag;", sum.getName(), ptr, self);
		emitter().emit("%s = (char*)((enum %s_tag_t*) ptr + 1);", ptr, sum.getName(), ptr);
		emitter().emit("switch (%s->tag) {", self);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s:", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				if (field.getType() instanceof AlgebraicType) {
					emitter().emit("write_%s_t(%s->data.%s.%s, %s);", ((AlgebraicType) field.getType()).getName(), self, variant.getName(), field.getName(), ptr);
					emitter().emit("%s += size_%s_t(%s->data.%s.%s);", ptr, ((AlgebraicType) field.getType()).getName(), self, variant.getName(), field.getName());
				} else {
					emitter().emit("*(%s*) %s = %s->data.%s.%s;", code().type(field.getType()), ptr, self, variant.getName(), field.getName());
					emitter().emit("%s = (char*)((%s*) %s + 1);", ptr, code().type(field.getType()), ptr);
				}
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineRead(SumType sum) {
		String self = "self";
		String ptr = "ptr";
		emitter().emit("%s* read_%s_t(char *buffer) {", code().type(sum), sum.getName());
		emitter().increaseIndentation();
		emitter().emit("if (!buffer) return NULL;");
		emitter().emit("char *%s = buffer;", ptr);
		emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(sum, self), sum.getName());
		emitter().emit("if (!%s) return NULL;", self);
		emitter().emit("%s->tag = *(enum %s_tag_t*) %s;", self,  sum.getName(), ptr);
		emitter().emit("%s = (char*)((enum %s_tag_t*) ptr + 1);", ptr, sum.getName(), ptr);
		emitter().emit("switch (%s->tag) {", self);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s:", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				if (field.getType() instanceof AlgebraicType) {
					emitter().emit("%s->data.%s.%s = read_%s_t(%s);", self, variant.getName(), field.getName(), ((AlgebraicType) field.getType()).getName(), ptr);
					emitter().emit("%s += size_%s_t(%s->data.%s.%s);", ptr, ((AlgebraicType) field.getType()).getName(), self, variant.getName(), field.getName());
				} else {
					emitter().emit("%s->data.%s.%s = *(%s*) %s;", self, variant.getName(), field.getName(), code().type(field.getType()), ptr);
					emitter().emit("%s = (char*)((%s*) %s + 1);", ptr, code().type(field.getType()), ptr);
				}
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("return self;");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineSize(SumType sum) {
		String self = "self";
		String size = "size";
		emitter().emit("size_t size_%s_t(%s) {", sum.getName(), code().declaration(sum, self));
		emitter().increaseIndentation();
		emitter().emit("if (!%s) return 0;", self);
		emitter().emit("size_t %s = 0;", size);
		emitter().emit("%s += sizeof(enum %s_tag_t);", size, sum.getName());
		emitter().emit("switch (%s->tag) {", self);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s:", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				if (field.getType() instanceof AlgebraicType) {
					emitter().emit("%s += size_%s_t(%s->data.%s.%s);", size, ((AlgebraicType) field.getType()).getName(), self, variant.getName(), field.getName());
				} else{
					emitter().emit("%s += sizeof(%s);", size, code().type(field.getType()));
				}
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("return %s;", size);
		emitter().decreaseIndentation();;
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineCopy(SumType sum) {
		String from = "from";
		String to = "to";
		emitter().emit("void copy_%s_t(%s, %s) {", sum.getName(), code().declaration(sum, "*" + to), code().declaration(sum, from));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !%s) return;", to, from);
		emitter().emit("if (*%s) { %s(*%s); *%s = NULL; }", to, destructor(sum), to, to);
		emitter().emit("if (!(*%s)) *%s = calloc(1, sizeof(%s_t));", to, to, sum.getName());
		emitter().emit("if (!(*%s)) return;", to);
		emitter().emit("(*%s)->tag = %s->tag;", to, from);
		emitter().emit("switch (%s->tag) {", from);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s:", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				String field1 = String.format("->data.%s.%s", variant.getName(), field.getName());
				code().copy(field.getType(), "(*" + to + ")" + field1, field.getType(), from + field1);
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineCompare(SumType sum) {
		String lhs = "lhs";
		String rhs = "rhs";
		emitter().emit("%s compare_%s_t(%s, %s) {", code().type(BoolType.INSTANCE), sum.getName(), code().declaration(sum, "lhs"), code().declaration(sum, "rhs"));
		emitter().increaseIndentation();
		emitter().emit("if (!%s || !%s) return false;", lhs, rhs);
		emitter().emit("if (%s->tag != %s->tag) return false;", lhs, rhs);
		emitter().emit("switch (%s->tag) {", lhs);
		emitter().increaseIndentation();
		sum.getVariants().forEach(variant -> {
			emitter().emit("case tag_%s_%s: {", sum.getName(), variant.getName());
			emitter().increaseIndentation();
			variant.getFields().forEach(field -> {
				emitter().emit("if (!%s) return false;", code().compare(field.getType(), String.format("%s->data.%s.%s", lhs, variant.getName(), field.getName()), field.getType(), String.format("%s->data.%s.%s", rhs, variant.getName(), field.getName())));
			});
			emitter().emit("break;");
			emitter().decreaseIndentation();
			emitter().emit("}");
		});
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("return true;");
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default String type(AlgebraicType type) {
		return type.getName() + "_t";
	}

	default String constructor(String constructor) {
		return types()
				.filter(type -> {
					if (type instanceof ProductType) {
						return Objects.equals(type.getName(), constructor);
					} else {
						return ((SumType) type).getVariants().stream().anyMatch(variant -> Objects.equals(variant.getName(), constructor));
					}
				})
				.map(type -> {
					if (type instanceof ProductType) {
						return "init_" + type(type);
					} else {
						return ((SumType) type).getVariants().stream().filter(variant -> Objects.equals(variant.getName(), constructor)).map(variant -> "init_" + type(type) + "_" + variant.getName()).findAny().get();
					}
				})
				.findAny()
				.get();
	}

	default String destructor(AlgebraicType type) {
		return String.format("free_%s_t", type.getName());
	}

	default Stream<AlgebraicType> types() {
		return backend().task()
				.getSourceUnits().stream()
				.flatMap(unit -> unit.getTree().getTypeDecls().stream())
				.map(decl -> (AlgebraicType) backend().types().declaredGlobalType(decl));
	}
}
