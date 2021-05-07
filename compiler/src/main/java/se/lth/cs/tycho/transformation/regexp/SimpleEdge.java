package se.lth.cs.tycho.transformation.regexp;

/**
 * This class defines an edge that can be used in JGrapht graphs. This class is
 * similar to UniqueEdge, but allows null labels.
 */
public class SimpleEdge {

    private Object object;

    /**
     * Creates a new edge with the given object.
     *
     * @param object an object
     */
    public SimpleEdge(Object object) {
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
        if (object == null) {
            return "";
        }
        return object.toString();
    }

}

