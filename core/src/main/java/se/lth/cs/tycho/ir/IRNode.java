package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The base type for all abstract syntax tree nodes.
 *
 * <p> Abstract syntax trees describe the syntactical structure of a program. Different syntactical elements of the
 * program are represented by different classes in the abstract syntax tree, all of which implements {@code IRNode}. The
 * tree is immutable, meaning they cannot change. A transformation of the tree is therefore carried out by creating a
 * new tree, possibly with subtrees shared with its predecessor.
 *
 * <p> The type hierarchy rooted in {@code IRNode} does not implement the visitor pattern. Instead, it relies on
 * external libraries, such as MultiJ, to provide extensibility and dynamic dispatch.
 *
 * <p> All non-abstract classes that implement this interface should have a public constructor with the children of the
 * node as parameters. A public {@code copy} method with the same parameters as the constructor should also be provided.
 * The {@code copy} method should return a node with the given children, and it should return the current node if the
 * children are the same as the current children.
 *
 * <p> Accessors for the children should be provided through normal get methods. The accessors should be accompanied by
 * "with" methods that takes a new child as parameter and returns a node with that child replaced, similar to
 * {@code copy}. If for example a node has a child "expression", then it should have a get method
 * {@code getExpression()} that returns the expression node and a with method {@code withExpression(Expression e)} that
 * returns a new node with the expression replaced by {@code e}.
 *
 * @see <a href="http://multij.org">MultiJ</a>
 */
public interface IRNode extends Cloneable {

	/**
	 * Executes an action on each child of this tree node.
	 * @param action the action to execute
	 */
	void forEachChild(Consumer<? super IRNode> action);

	/**
	 * Applies a transformation on each child and returns a new node with transformed children.
	 * If the transformation does not transform the children, the current node should be returned.
	 *
	 * @param transformation the transformation
	 * @return a new node with transformed children
	 */
	IRNode transformChildren(Transformation transformation);

	/**
	 * Returns a stream of nodes of a walk of the tree rooted in this node.
	 * @return a stream of nodes
	 */
	default Stream<IRNode> walk() {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new IRNodeIterator(this),
						Spliterator.IMMUTABLE),
				false);
	}

	/**
	 * Returns the start line number of the source code that is represented by this node.
	 * @return the start line number
	 */
	default int getFromLineNumber() {
		return 0;
	}

	/**
	 * Returns the start column number of the source code that is represented by this node.
	 * @return the start column number
	 */
	default int getFromColumnNumber() {
		return 0;
	}

	/**
	 * Returns the end line number of the source code that is represented by this node.
	 * @return the end line number
	 */
	default int getToLineNumber() {
		return 0;
	}

	/**
	 * Returns the end column number of the source code that is represented by this node.
	 * @return the end column number
	 */
	default int getToColumnNumber() {
		return 0;
	}

	/**
	 * Indicates whether this node has information about its position in the source code.
	 * @return true if this node has position information
	 */
	default boolean hasPosition() {
		return getFromLineNumber() > 0 && getFromColumnNumber() > 0 && getToLineNumber() > 0 && getToColumnNumber() > 0;
	}

	/**
	 * Returns a clone of this node.
	 * @return a clone
	 */
	IRNode clone();

	/**
	 * Returns a clone of this subtree.
	 * @return a deep clone
	 */
	default IRNode deepClone() {
		return clone().transformChildren(IRNode::deepClone);
	}

	/**
	 * A functional interface that describes a transformation of an {@link IRNode}.
	 */
	@FunctionalInterface
	interface Transformation extends Function<IRNode, IRNode> {

		/**
		 * Returns a transformed node.
		 * The transformed node should (typically) be of the same type as the parameter.
		 * For some uses, however, a node may be transformed to a sibling in the type hierarchy,
		 * for example a {@link se.lth.cs.tycho.ir.expr.ExprUnaryOp} might be transformed to a
		 * {@link se.lth.cs.tycho.ir.expr.ExprApplication}.
		 *
		 * @param node the node to transform
		 * @return the transformed node
		 */
		IRNode apply(IRNode node);

		/**
		 * Transformed a node with {@link #apply} and checks the type of the result.
		 *
		 * @param type the expected result type
		 * @param node the node to transform
		 * @param <T> the expected type
		 * @return a transformed node of the expected type
		 */
		default <T extends IRNode> T applyChecked(Class<T> type, T node) {
			return type.cast(apply(node));
		}

		/**
		 * Applies the transformation on all elements of a list and checks the type of the result.
		 *
		 * @param type the expected element type
		 * @param nodes the list to be transformed
		 * @param <T> the expected type
		 * @return the transformed list
		 */
		default <T extends IRNode> List<T> mapChecked(Class<T> type, List<T> nodes) {
			return nodes.stream()
					.map(this)
					.map(type::cast)
					.collect(ImmutableList.collector());
		}
	}
}


