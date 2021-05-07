package se.lth.cs.tycho.transformation.regexp;

/**
 * This class defines an edge that can be used in JGrapht graphs. This class is
 * necessary because edges need to be unique (even when using a multi-graph). So
 * this class simply wraps an object, but uses Object's equal and hashCode
 * methods, meaning comparison by reference.
 */
public class UniqueEdge {
    private Object object;

    /**
     * Creates a new edge with the given object.
     *
     * @param object an object
     * @throws NullPointerException if tag is <code>null</code>
     */
    public UniqueEdge(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.object = object;
    }

    /**
     * Returns the object associated with this edge.
     *
     * @return the object associated with this edge
     */
    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
