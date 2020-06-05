package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;

public abstract class VarDecl extends AbstractDecl {

    private final TypeExpr type;
    private final Expression value;
    private final boolean constant;
    private final boolean external;
    private final ImmutableList<Annotation> annotations;

    protected VarDecl(VarDecl original, List<Annotation> annotations, TypeExpr type, String name, Expression value, boolean constant, boolean external) {
        super(original, name);
        this.annotations = ImmutableList.from(annotations);
        this.type = type;
        this.value = value;
        this.constant = constant;
        this.external = external;
    }

    public static GlobalVarDecl global(List<Annotation> annotations, Availability availability, TypeExpr type, String name, Expression value) {
        return new GlobalVarDecl(annotations, availability, type, name, value);
    }

    public static LocalVarDecl local(List<Annotation> annotations, TypeExpr type, String name, Expression value, boolean constant) {
        return new LocalVarDecl(annotations, type, name, value, constant);
    }

    public static ParameterVarDecl parameter(List<Annotation> annotations, TypeExpr type, String name, Expression defaultValue) {
        return new ParameterVarDecl(annotations, type, name, defaultValue);
    }

    public static InputVarDecl input(String name) {
        return new InputVarDecl(name);
    }

    public static GeneratorVarDecl generator(String name) {
        return new GeneratorVarDecl(name);
    }

    @Override
    public abstract VarDecl withName(String name);

    public TypeExpr getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Expression getValue() {
        return value;
    }

    public boolean isConstant() {
        return constant;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public abstract VarDecl transformChildren(Transformation transformation);

}