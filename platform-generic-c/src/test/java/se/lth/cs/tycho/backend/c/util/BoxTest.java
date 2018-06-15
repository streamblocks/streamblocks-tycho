package se.lth.cs.tycho.backend.c.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoxTest {
	private static final String content1 = "abc";
	private static final String content2 = "xyz";

	@Test
	public void of() {
		assertSame(Box.of(content1).get(), content1);
	}

	@Test(expected = NullPointerException.class)
	public void ofNull() {
		Box.of(null);
	}

	@Test
	public void ofNullable() {
		assertTrue(Box.ofNullable(null).isEmpty());
		assertSame(Box.ofNullable(content1).get(), content1);
	}

	@Test
	public void empty() {
		assertTrue(Box.empty().isEmpty());
	}

	@Test
	public void isEmpty() {
		assertFalse(Box.of(content1).isEmpty());
		assertTrue(Box.empty().isEmpty());
	}

	@Test
	public void get() {
		assertSame(Box.of(content1).get(), content1);
	}

	@Test(expected = IllegalStateException.class)
	public void getEmpty() {
		Box.empty().get();
	}

	@Test
	public void set() {
		Box<String> box = Box.empty();
		box.set(content1);
		assertSame(box.get(), content1);
		box.set(content2);
		assertSame(box.get(), content2);
	}

	@Test(expected = NullPointerException.class)
	public void setNull() {
		Box.of(content1).set(null);
	}

	@Test
	public void clear() {
		Box<String> box = Box.of(content1);
		box.clear();
		assertTrue(box.isEmpty());
		Box<String> empty = Box.empty();
		empty.clear();
		assertTrue(box.isEmpty());
	}

	@Test
	public void testHashCode() {
		assertEquals(Box.empty().hashCode(), Box.empty().hashCode());
		assertEquals(Box.of(content1).hashCode(), Box.of(content1).hashCode());
		assertNotEquals(content1.hashCode(), content2.hashCode());
		assertNotEquals(Box.of(content1).hashCode(), Box.of(content2).hashCode());
	}

	@Test
	public void testEquals() {
		assertTrue(Box.of(content1).equals(Box.of(content1)));
		assertFalse(Box.of(content1).equals(Box.of(content2)));
		assertFalse(Box.of(content1).equals(Box.empty()));
		assertFalse(Box.empty().equals(Box.of(content1)));
		assertTrue(Box.empty().equals(Box.empty()));
	}

}