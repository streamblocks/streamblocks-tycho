package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class represents annotation parameters nodes in the abstract syntax trees.
 * An annotation parameter has an expression and might have a name.
 */
public class AnnotationParameter extends AbstractIRNode {
    private final String name;
    private final Expression expression;

    /**
     * Constructs an annotation parameter node.
     *
     * @param name       the name of the annotation parameter
     * @param expression the annotation parameter
     */
    public AnnotationParameter(String name, Expression expression) {
        this(null, name, expression);
    }

    /**
     * Constructs an annotation parameter node from a previous node.
     *
     * @param original   the previous node
     * @param name       the name of the annotation parameter
     * @param expression the annotatio parameter expression
     */
    public AnnotationParameter(AnnotationParameter original, String name, Expression expression) {
        super(original);
        this.name = name;
        this.expression = expression;
    }

    /**
     * Returns the name of the annotation parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the expression of the annotation parameter.
     *
     * @return the name
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Annotation) {
            AnnotationParameter that = (AnnotationParameter) obj;
            return this.name.equals(that.name) && this.expression.equals(that.expression);
        } else {
            return false;
        }
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, expression);
    }

    @Override
    public IRNode transformChildren(Transformation transformation) {
        return this;
    }

    @Override
    public AnnotationParameter deepClone() {
        return (AnnotationParameter) super.deepClone();
    }
}
