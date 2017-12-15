package se.lth.cs.tycho.attribute.util;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static se.lth.cs.tycho.attribute.util.Attributes.circular;

public class CircularTest {
	private final Supplier<Integer> a;
	private final Supplier<Integer> b;
	private final Function<Integer, Integer> c;
	private final int value;

	public CircularTest() {
		value = 1000;
		a = Attributes.circular(0, () -> Math.min(b().get() + 1, value));
		b = Attributes.circular(0, () -> a().get());

		c = Attributes.circular(0, x -> x == value ? x : c().apply(x+1));
	}

	public Supplier<Integer> a() {
		return a;
	}
	public Supplier<Integer> b() {
		return b;
	}

	public Function<Integer, Integer> c() {
		return c;
	}


	@Test
	public void testAB() {
		assertEquals("Wrong value", Integer.valueOf(value), a().get());
		assertEquals("Wrong value", Integer.valueOf(value), b().get());
	}

	@Test
	public void testC() {
		assertEquals("Wrong value", Integer.valueOf(value), c().apply(0));
	}

}
