package se.lth.cs.tycho.ir;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * This class represents annotation nodes in the abstract syntax trees.
 * An annotation has a name and a list of key-value pairs.
 */
public class Annotation extends AbstractIRNode {
    private final String name;
    private final ImmutableList<AnnotationParameter> parameters;

    /**
     * Constructs an annotation node.
     *
     * @param name       the name of the anotation
     * @param parameters the list of key-value pairs
     */
    public Annotation(String name, ImmutableList<AnnotationParameter> parameters) {
        this(null, name, parameters);
    }

    /**
     * Constructs an annotation node from a previous node.
     *
     * @param original   the previous node
     * @param name       the name of the annotation
     * @param parameters the list of key-value pairs
     */
    private Annotation(Annotation original, String name, ImmutableList<AnnotationParameter> parameters) {
        super(original);
        this.name = name;
        this.parameters = parameters;
    }

    /**
     * Check if a list of annotations has an annotation with a given name
     *
     * @param name        the annotation name to search
     * @param annotations the list of annotations
     * @return true if the annotation is found
     */
    public static boolean hasAnnotationWithName(String name, List<Annotation> annotations) {
        return annotations.stream().filter(ann -> ann.getName().equals(name)).findAny().isPresent();
    }

    /**
     * Get the annotation from a given annotation name
     *
     * @param name        the annotation name to search
     * @param annotations the list of annotations
     * @return an optional annotation
     */
    public static Optional<Annotation> getAnnotationWithName(String name, List<Annotation> annotations) {
        return annotations.stream().filter(ann -> ann.getName().equals(name)).findAny();
    }

    /**
     * Returns the name of the annotation.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the key-value pairs.
     *
     * @return the key-value pairs
     */
    public ImmutableList<AnnotationParameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Annotation) {
            Annotation that = (Annotation) obj;
            return this.name.equals(that.name) && this.parameters.equals(that.parameters);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Annotation transformChildren(Transformation transformation) {
        return this;
    }

    @Override
    public Annotation deepClone() {
        return (Annotation) super.deepClone();
    }
}
