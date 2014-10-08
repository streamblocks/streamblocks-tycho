package se.lth.cs.tycho.classifier.util;

import java.util.BitSet;
import java.util.NoSuchElementException;


public class ImmutableBitSet implements Iterable<Integer> {
	
	private final BitSet delegate;
	
	private ImmutableBitSet(BitSet set) {
		delegate = set;
	}
	
	public static ImmutableBitSet fromBitSet(BitSet set) {
		return new ImmutableBitSet(copyOf(set));
	}
	
	private static BitSet copyOf(BitSet set) {
		BitSet copy = new BitSet();
		copy.or(set);
		return copy;
	}
	
	public boolean intersects(ImmutableBitSet set) {
		return delegate.intersects(set.delegate);
	}
	
	public ImmutableBitSet set(int bitIndex, boolean value) {
		BitSet set = copyOf(delegate);
		set.set(bitIndex, value);
		return new ImmutableBitSet(set);
	}
	
	public boolean get(int bitIndex) {
		return delegate.get(bitIndex);
	}

	private static ImmutableBitSet EMPTY_BIT_SET = new ImmutableBitSet(new BitSet());
	
	public static ImmutableBitSet empty() {
		return EMPTY_BIT_SET;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ImmutableBitSet)) {
			return false;
		}
		ImmutableBitSet other = (ImmutableBitSet) obj;
		if (delegate == null) {
			if (other.delegate != null) {
				return false;
			}
		} else if (!delegate.equals(other.delegate)) {
			return false;
		}
		return true;
	}

	public ImmutableBitSet or(ImmutableBitSet set) {
		BitSet result = copyOf(delegate);
		result.or(set.delegate);
		return new ImmutableBitSet(result);
	}
	
	private class Iterator implements java.util.Iterator<Integer> {
		private int bit = delegate.nextSetBit(0);

		@Override
		public boolean hasNext() {
			return bit >= 0;
		}

		@Override
		public Integer next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int result = bit;
			bit = delegate.nextSetBit(bit + 1);
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

	@Override
	public java.util.Iterator<Integer> iterator() {
		return new Iterator();
	}
}
