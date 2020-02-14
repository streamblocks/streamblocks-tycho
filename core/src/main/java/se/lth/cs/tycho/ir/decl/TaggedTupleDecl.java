package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class TaggedTupleDecl extends AbstractDecl {

    private List<FieldVarDecl> fields;

    public TaggedTupleDecl(String name, List<FieldVarDecl> fields) {
        this(null, name, fields);
    }

    public TaggedTupleDecl(AbstractDecl original, String name, List<FieldVarDecl> fields) {
        super(original, name);
        this.fields = ImmutableList.from(fields);
    }

    public List<FieldVarDecl> getFields() {
        return fields;
    }

    public TaggedTupleDecl withFields(List<FieldVarDecl> fields) {
        return copy(getName(), fields);
    }

    @Override
    public Decl withName(String name) {
        return copy(name, getFields());
    }

    private TaggedTupleDecl copy(String name, List<FieldVarDecl> fields) {
        if (Objects.equals(getName(), name) && Objects.equals(getFields(), fields)) {
            return this;
        } else {
            return new TaggedTupleDecl(this, name, fields);
        }
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        if (getFields() != null) getFields().forEach(action);
    }

    @Override
    public Decl transformChildren(Transformation transformation) {
        return copy(getName(), getFields() == null ? null : transformation.mapChecked(FieldVarDecl.class, getFields()));
    }
}
