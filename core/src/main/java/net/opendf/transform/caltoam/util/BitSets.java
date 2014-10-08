package net.opendf.transform.caltoam.util;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class BitSets {
	public static BitSet fromIterable(Iterable<Integer> ints) {
		BitSet bitSet = new BitSet();
		for (Integer i : ints) {
			if (i != null) {
				bitSet.set(i);
			}
		}
		return bitSet;
	}
	
	public static BitSet copyOf(BitSet original) {
		BitSet copy = new BitSet();
		if (original == null) return copy;
		copy.or(original);
		return copy;
	}
	
	public static BitSet fromBigInteger(BigInteger bigInteger) {
		if (bigInteger.signum() == -1) {
			throw new IllegalArgumentException("Undefined for negative values");
		}
		byte[] array = bigInteger.toByteArray();
		reverse(array);
		return BitSet.valueOf(array);
	}
	
	public static BigInteger toBigInteger(BitSet bitSet) {
		byte[] array = bitSet.toByteArray();
		reverse(array);
		return new BigInteger(1, array);
	}
	
	private static void reverse(byte[] array) {
		final int mid = array.length/2;
		int a = 0;
		int b = array.length-1;
		while (a < mid) {
			byte aVal = array[a];
			array[a] = array[b];
			array[b] = aVal;
			a += 1;
			b -= 1;
		}
	}
	
	public static Iterable<Integer> iterable(BitSet bitSet) {
		return new BitSetIterable(bitSet);
	}
	
	public static Iterator<Integer> iterator(BitSet bitSet) {
		return new BitSetIterator(bitSet);
	}
	
	private static class BitSetIterable implements Iterable<Integer> {
		private final BitSet bitSet;
		
		private BitSetIterable(BitSet bitSet) {
			this.bitSet = bitSet;
		}
		
		@Override
		public Iterator<Integer> iterator() {
			return new BitSetIterator(bitSet);
		}
	}
	
	private static class BitSetIterator implements Iterator<Integer> {
		private final BitSet bitSet;
		private int next;
		private int prev;
		
		private BitSetIterator(BitSet bitSet) {
			this.bitSet = bitSet;
			next = bitSet.nextSetBit(0);
			prev = -1;
		}
		
		@Override
		public boolean hasNext() {
			return next >= 0;
		}
		
		@Override
		public Integer next() {
			if (!hasNext()) {
				prev = -1;
				throw new NoSuchElementException();
			}
			prev = next;
			next = bitSet.nextSetBit(next+1);
			return prev;
		}
		
		@Override
		public void remove() {
			if (prev >= 0 && bitSet.get(prev)) {
				bitSet.clear(prev);
				prev = -1;
			} else {
				throw new IllegalStateException();
			}
		}
	}
	
	public static void main(String[] args) {
		BitSet bs = new BitSet();
		Set<Integer> is = new TreeSet<Integer>();
		Random rng = new Random();
		final int generated = 10000;
		final int range = 10000;
		int remove = 1000;
		for (int i = 0; i < generated; i++) {
			int r = rng.nextInt(range);
			bs.set(r);
			is.add(r);
		}
		Iterator<Integer> bsr = iterator(bs);
		Iterator<Integer> isr = is.iterator();
		int counter = bs.cardinality();
		if (counter < remove) remove = counter;
		while(bsr.hasNext() && isr.hasNext()) {
			bsr.next();
			isr.next();
			if (rng.nextInt(counter--) < remove) {
				remove--;
				bsr.remove();
				isr.remove();
			}
		}
		
		Iterator<Integer> bsi = iterator(bs);
		Iterator<Integer> isi = is.iterator();
		
		while(bsi.hasNext() && isi.hasNext()) {
			if ((int) bsi.next() != (int) isi.next()) {
				System.err.println("Different entries");
				return;
			}
		}
		if (bsi.hasNext() || isi.hasNext()) {
			System.err.println("Different lengths");
			return;
		}
		System.out.println("Okay");
	}
}
