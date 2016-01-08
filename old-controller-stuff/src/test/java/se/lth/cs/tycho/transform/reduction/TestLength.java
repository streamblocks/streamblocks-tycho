package se.lth.cs.tycho.transform.reduction;

import static org.junit.Assert.*;

import org.junit.Test;

import se.lth.cs.tycho.transform.reduction.util.Length;

public class TestLength {
	@Test
	public void testCompareExecExec() {
		assertEquals(0, Length.comparator().compare(Length.execLength(0.5), Length.execLength(0.5)));
		assertEquals(-1, Length.comparator().compare(Length.execLength(1), Length.execLength(0)));
		assertEquals(1, Length.comparator().compare(Length.execLength(0), Length.execLength(1)));
	}
	
	@Test
	public void testCompareExecTest() {
		assertEquals(1, Length.comparator().compare(Length.execLength(0), Length.testLength(Length.execLength(1))));
		assertEquals(-1, Length.comparator().compare(Length.execLength(1), Length.testLength(Length.execLength(0))));
	}
	
	@Test
	public void testCompareExecWait() {
		assertEquals(1, Length.comparator().compare(Length.waitLength(), Length.execLength(0)));
		assertEquals(1, Length.comparator().compare(Length.waitLength(), Length.execLength(1)));
		assertEquals(-1, Length.comparator().compare(Length.execLength(0), Length.waitLength()));
		assertEquals(-1, Length.comparator().compare(Length.execLength(1), Length.waitLength()));
	}
	
	@Test
	public void testompareTestTest() {
		assertEquals(0, Length.comparator().compare(Length.testLength(Length.waitLength()), Length.testLength(Length.waitLength())));
		assertEquals(0, Length.comparator().compare(Length.testLength(Length.execLength(1)), Length.testLength(Length.execLength(1))));
		assertEquals(-1, Length.comparator().compare(Length.testLength(Length.execLength(1)), Length.testLength(Length.testLength(Length.execLength(1)))));
	}
	
}
