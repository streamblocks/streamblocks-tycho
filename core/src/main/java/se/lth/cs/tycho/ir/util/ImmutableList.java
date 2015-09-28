package se.lth.cs.tycho.ir.util;

import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

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

	public <F> ImmutableList<F> map(Function<? super E, ? extends F> function) {
		F transformed = null;
		int i = 0;
		while (i < size) {
			E original = list[i];
			transformed = function.apply(original);
			if (original != transformed) {
				break;
			}
			i += 1;
		}
		if (i == size) {
			return (ImmutableList<F>) this;
		} else {
			Object[] newList = new Object[size];
			System.arraycopy(list, 0, newList, 0, i);
			newList[i] = transformed;
			i += 1;
			while (i < size) {
				newList[i] = function.apply(list[i]);
				i += 1;
			}
			return new ImmutableList<F>(newList, size);
		}
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
	public void forEach(Consumer<? super E> action) {
		for (int i = 0; i < size; i++) {
			action.accept(list[i]);
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
	 * Returns a list with value v.
	 * 
	 * @param v
	 *            the list element
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E v) {
		return new ImmutableList<E>(new Object[] { v }, 1);
	}

	/**
	 * Returns a list with the specified values.
	 *
	 * @return a list
	 */
	public static <E> ImmutableList<E> of(E... values) {
		if (values == null) {
			return empty();
		}
		return new Builder<E>().addAll(values).build();
	}

	/**
	 * Returns a list containing the elements of elements, and an empty list
	 * list if elements is null. If elements is an ImmutableList, this method
	 * will return elements.
	 * 
	 * @param collection
	 *            the content of the list
	 * @return a list containing the elements of the argument
	 */
	public static <E> ImmutableList<E> from(Collection<E> collection) {
		if (collection == null) {
			return empty();
		} else if (collection instanceof ImmutableList) {
			return (ImmutableList<E>) collection;
		} else {
			return new ImmutableList<>(collection.toArray(), collection.size());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <E> ImmutableList<E> covariance(ImmutableList<? extends E> list) {
		return (ImmutableList<E>) list;
	}
	
	public static <E> Collector<E, Builder<E>, ImmutableList<E>> collector() {
		Supplier<Builder<E>> supplier = Builder::new;
		BiConsumer<Builder<E>, E> accumulator = Builder::add;
		BinaryOperator<Builder<E>> combiner = (b1, b2) -> b1.addAll(b2.build());
		Function<Builder<E>, ImmutableList<E>> finisher = Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher); 
	}

	public static <A, B extends A, C extends A> ImmutableList<A> concat(ImmutableList<B> init, ImmutableList<C> tail) {
		if (init.isEmpty()) {
			return covariance(tail);
		} else if (tail.isEmpty()) {
			return covariance(init);
		} else {
			Object[] list = new Object[init.size() + tail.size()];
			System.arraycopy(init.list, 0, list, 0, init.size());
			System.arraycopy(tail.list, 0, list, init.size(), tail.size());
			return new ImmutableList<>(list, list.length);
		}
	}

	/**
	 * A class for constructing ImmutableLists.
	 * 
	 * @param <E>
	 */
	public static final class Builder<E> implements Consumer<E> {
		private Object[] list;
		private int size;
		private static final int INIT_SIZE = 10;

		/**
		 * Constructs an empty builder.
		 */
		public Builder() {
			list = new Object[INIT_SIZE];
			size = 0;
		}

		private void resizeArray() {
			list = Arrays.copyOf(list, list.length + list.length >> 1);
		}

		private boolean isFull() {
			return size == list.length;
		}

		public void accept(E element) {
			add(element);
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
		public Builder<E> addAll(Iterable<? extends E> elements) {
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
