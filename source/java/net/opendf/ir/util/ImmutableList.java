package net.opendf.ir.util;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * Immutable lists.
 * 
 * @param <E>
 */
public final class ImmutableList<E> extends AbstractList<E> {

	private final E[] list;
	private final int size;

	private ImmutableList(Object[] list, int size) {
		this.list = (E[]) list;
		this.size = size;
	}

	@Override
	public E get(int index) {
		checkBounds(index);
		return list[index];
	}

	private void checkBounds(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Constructs a Builder for building ImmutableLists
	 * 
	 * @return a Builder
	 */
	public static <E> Builder<E> builder() {
		return new Builder<E>();
	}

	private static final ImmutableList EMPTY_LIST = new ImmutableList(null, 0);

	/**
	 * Returns an empty list.
	 * 
	 * @return an empty list.
	 */
	public static <E> ImmutableList<E> empty() {
		return EMPTY_LIST;
	}

	/**
	 * Returns a list with element e0.
	 * 
	 * @param e0
	 *            the list element
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E e0) {
		return new ImmutableList<E>(new Object[] { e0 }, 1);
	}

	/**
	 * Returns a list of two elements: e0 and e1.
	 * 
	 * @param e0
	 *            the first element
	 * @param e1
	 *            the second element
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E e0, E e1) {
		return new ImmutableList<E>(new Object[] { e0, e1 }, 2);
	}

	/**
	 * Returns a list of three elements: e0, e1 and e2.
	 * 
	 * @param e0
	 *            the first element
	 * @param e1
	 *            the second element
	 * @param e2
	 *            the third element
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E e0, E e1, E e2) {
		return new ImmutableList<E>(new Object[] { e0, e1, e2 }, 3);
	}

	/**
	 * Returns a list of the elements e0, e1 and e2 followed by the elements of
	 * tail.
	 * 
	 * @param e0
	 *            the first element
	 * @param e1
	 *            the second element
	 * @param e2
	 *            the third element
	 * @param tail
	 *            the last elements
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E e0, E e1, E e2, E... tail) {
		int length = tail.length + 3;
		Object[] array = new Object[length];
		array[0] = e0;
		array[1] = e1;
		array[2] = e2;
		System.arraycopy(tail, 0, array, 3, tail.length);
		return new ImmutableList<E>(array, length);
	}

	/**
	 * Returns a list containing the elements of elements, and an empty list
	 * list if elements is null. If elements is an ImmutableList, this method
	 * will return elements.
	 * 
	 * @param elements
	 *            the content of the list
	 * @return a list containing the elements of the argument
	 */
	public static <E> ImmutableList<E> copyOf(Iterable<E> elements) {
		if (elements == null) {
			return empty();
		} else if (elements instanceof ImmutableList) {
			return (ImmutableList<E>) elements;
		} else {
			return new Builder<E>().addAll(elements).build();
		}
	}

	/**
	 * Returns a list containing the elements of elements, and an empty list if
	 * elements is null.
	 * 
	 * @param elements
	 *            an array of elements
	 * @return a list containing the elements of the argument
	 */
	public static <E> ImmutableList<E> copyOf(E[] elements) {
		if (elements == null) {
			return empty();
		}
		return new Builder<E>().addAll(elements).build();
	}

	/**
	 * A class for constructing ImmutableLists.
	 * 
	 * @param <E>
	 */
	public static final class Builder<E> {
		private Object[] list;
		private int size;
		private static final int INIT_SIZE = 8;

		/**
		 * Constructs an empty builder.
		 */
		public Builder() {
			list = new Object[INIT_SIZE];
			size = 0;
		}

		private void resizeArray() {
			list = Arrays.copyOf(list, list.length * 2);
		}

		private boolean isFull() {
			return size == list.length;
		}

		/**
		 * Adds element to the end of the list.
		 * 
		 * @param element
		 * @return the builder
		 */
		public Builder<E> add(E element) {
			if (isFull()) {
				resizeArray();
			}
			list[size++] = element;
			return this;
		}

		/**
		 * Adds the elements of elements to the list.
		 * 
		 * @param elements
		 * @return the builder
		 */
		public Builder<E> addAll(Iterable<E> elements) {
			for (E e : elements) {
				add(e);
			}
			return this;
		}

		/**
		 * Adds the elements of elements to the list.
		 * 
		 * @param elements
		 * @return the builder
		 */
		public Builder<E> addAll(E... elements) {
			return addAll(Arrays.asList(elements));
		}

		/**
		 * Returns an ImmutableList containing the elements added to this
		 * builder.
		 * 
		 * @return a list
		 */
		public ImmutableList<E> build() {
			return new ImmutableList<E>(list, size);
		}
	}

}
