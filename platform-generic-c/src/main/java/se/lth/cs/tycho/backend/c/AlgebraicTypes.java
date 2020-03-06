package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.AlgebraicType;
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

	default void declareAlgebraicTypes() {
		emitter().emit("// TYPE DECLARATIONS");
		types().forEachOrdered(this::declareType);
	}

	default void declareType(AlgebraicType type) {}

	default void declareType(ProductType product) {
		emitter().emit("typedef struct %s_s %s_t;", product.getName(), product.getName());
		emitter().emit("");
		emitter().emit("struct %s_s {", product.getName());
		emitter().increaseIndentation();
		product.getFields().forEach(field -> {
			emitter().emit("%s;",  code().declaration(field.getType(), field.getName()));
		});
		emitter().decreaseIndentation();
		emitter().emit("};");
		emitter().emit("");
		emitter().emit("%s* init_%s(%s);", code().type(product), product.getName(), product.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		emitter().emit("");
		emitter().emit("void write_%s(%s, char *buffer);", product.getName(), code().declaration(product, "self"));
		emitter().emit("");
		emitter().emit("%s* read_%s(char *buffer);", code().type(product), product.getName());
		emitter().emit("");
		emitter().emit("size_t size_%s(%s);", product.getName(), code().declaration(product, "self"));
		emitter().emit("");
	}

	default void declareType(SumType sum) {
		emitter().emit("typedef struct %s_s %s_t;", sum.getName(), sum.getName());
		emitter().emit("");
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
			emitter().emit("%s* init_%s_%s(%s);", code().type(sum), sum.getName(), variant.getName(), variant.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		});
		emitter().emit("");
		emitter().emit("void write_%s(%s, char *buffer);", sum.getName(), code().declaration(sum, "self"));
		emitter().emit("");
		emitter().emit("%s* read_%s(char *buffer);", code().type(sum), sum.getName());
		emitter().emit("");
		emitter().emit("size_t size_%s(%s);", sum.getName(), code().declaration(sum, "self"));
		emitter().emit("");
	}

	default void defineAlgebraicTypes() {
		emitter().emit("// TYPE DEFINITIONS");
		types().forEachOrdered(this::defineType);
	}

	default void defineType(AlgebraicType type) {}

	default void defineType(ProductType product) {
		defineInit(product);
	}

	default void defineInit(ProductType product) {
		String variable = "self";
		emitter().emit("%s* init_%s(%s) {", code().type(product), product.getName(), product.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
		emitter().increaseIndentation();
		emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(product, variable), product.getName());
		product.getFields().forEach(field -> emitter().emit("%s->%s = %s;", variable, field.getName(), field.getName()));
		emitter().emit("return %s;", variable);
		emitter().decreaseIndentation();
		emitter().emit("}");
		emitter().emit("");
	}

	default void defineType(SumType sum) {
		defineInit(sum);
	}

	default void defineInit(SumType sum) {
		sum.getVariants().forEach(variant -> {
			String self = "self";
			emitter().emit("%s* init_%s_%s(%s) {", code().type(sum), sum.getName(), variant.getName(), variant.getFields().stream().map(field -> code().declaration(field.getType(), field.getName())).collect(Collectors.joining(", ")));
			emitter().increaseIndentation();
			emitter().emit("%s = calloc(1, sizeof(%s_t));", code().declaration(sum, self), sum.getName());
			emitter().emit("%s->tag = tag_%s_%s;", self, sum.getName(), variant.getName());
			variant.getFields().forEach(field -> {
				emitter().emit("%s->data.%s.%s = %s;", self, variant.getName(), field.getName(), field.getName());
			});
			emitter().emit("return %s;", self);
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		});
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
						return "init_" + type.getName();
					} else {
						return ((SumType) type).getVariants().stream().filter(variant -> Objects.equals(variant.getName(), constructor)).map(variant -> "init_" + type.getName() + "_" + variant.getName()).findAny().get();
					}
				})
				.findAny()
				.get();
	}

	default Stream<AlgebraicType> types() {
		return backend().task()
				.getSourceUnits().stream()
				.flatMap(unit -> unit.getTree().getTypeDecls().stream())
				.map(decl -> (AlgebraicType) backend().types().declaredGlobalType(decl));
	}
}
