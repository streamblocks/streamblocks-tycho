package se.lth.cs.tycho.backend.c;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.type.UserType;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public interface UserTypes {
	@Binding(BindingKind.INJECTED)
	Backend backend();

	default Emitter emitter() {
		return backend().emitter();
	}

	default Code code() {
		return backend().code();
	}

	default void declareUserTypes() {
		emitter().emit("// TYPE DECLARATIONS");
		userTypes().forEachOrdered(this::declareType);
	}

	default void declareType(UserType type) {
		emitter().emit("typedef struct %s_s %s_t;", type.getName(), type.getName());
		emitter().emit("");
		emitter().emit("enum %s_type {", type.getName());
		emitter().increaseIndentation();
		type.getRecords().forEach(record -> {
			emitter().emit("type_%s_%s%s",
					type.getName(),
					record.getName() == null ? type.getName() : record.getName(),
					type.getRecords().indexOf(record) == type.getRecords().size() - 1 ? "" : ",");
		});
		emitter().decreaseIndentation();
		emitter().emit("};");
		emitter().emit("");
		emitter().emit("struct %s_s {", type.getName());
		emitter().increaseIndentation();
		emitter().emit("enum %s_type type;", type.getName());
		emitter().emit("union {");
		emitter().increaseIndentation();
		type.getRecords().forEach(record -> {
			emitter().emit("struct {");
			emitter().increaseIndentation();
			record.getFields().forEach(field -> {
				emitter().emit("%s %s;", code().type(field.getType()), field.getName());
			});
			emitter().decreaseIndentation();
			emitter().emit("} %s;", record.getName() == null ? type.getName() : record.getName());
		});
		emitter().decreaseIndentation();
		emitter().emit("} self;");
		emitter().decreaseIndentation();
		emitter().emit("};");
		type.getRecords().forEach(record -> {
			emitter().emit("");
			emitter().emit("%s_t init_%s_%s(%s);",
					type.getName(),
					type.getName(),
					record.getName() == null ? type.getName() : record.getName(),
					record.getFields()
							.stream()
							.map(field -> code().declaration(field.getType(), field.getName()))
							.collect(Collectors.joining(", ")));
		});
		emitter().emit("");
	}

	default Stream<UserType> userTypes() {
		return backend().task()
				.getSourceUnits().stream()
				.flatMap(unit -> unit.getTree().getTypeDecls().stream())
				.map(decl -> (UserType) backend().types().declaredGlobalType(decl));
	}

	default void defineUserTypes() {
		userTypes().forEachOrdered(this::defineUserType);
	}

	default void defineUserType(UserType type) {
		type.getRecords().forEach(record -> {
			String variable = "_";
			String self = record.getName() == null ? type.getName() : record.getName();
			emitter().emit("");
			emitter().emit("%s_t init_%s_%s(%s) {",
					type.getName(),
					type.getName(),
					self,
					record.getFields()
							.stream()
							.map(field -> code().declaration(field.getType(), field.getName()))
							.collect(Collectors.joining(", ")));
			emitter().increaseIndentation();
			emitter().emit("%s_t %s;", type.getName(), variable);
			emitter().emit("%s.type = type_%s_%s;", variable, type.getName(), self);
			for (int i = 0; i < record.getFields().size(); ++i) {
				String field = record.getFields().get(i).getName();
				emitter().emit("%s.self.%s.%s = %s;", variable, self, field, field);
			}
			emitter().emit("return %s;", variable);
			emitter().decreaseIndentation();
			emitter().emit("}");
			emitter().emit("");
		});
	}

	default String type(UserType type) {
		return type.getName() + "_t";
	}

	default String constructor(String type, String constructor) {
		return "init_" + type + "_" + (constructor == null ? type : constructor);
	}
}
