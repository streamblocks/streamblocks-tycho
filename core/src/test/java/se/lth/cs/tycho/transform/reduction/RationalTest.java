/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.lth.cs.tycho.transform.reduction;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Test;

import se.lth.cs.tycho.transform.reduction.util.Rational;
import static org.junit.Assert.*;

/**
 *
 * @author gustav
 */
public class RationalTest {

	/**
	 * Test of valueOf method, of class Rational.
	 */
	@Test
	public void testValueOf_long_long() {
		long numerator = 10L;
		long denominator = 5L;
		Rational expResult = Rational.valueOf(2);
		Rational result = Rational.valueOf(numerator, denominator);
		assertEquals(expResult, result);
	}

	/**
	 * Test of valueOf method, of class Rational.
	 */
	@Test
	public void testValueOf_BigInteger_BigInteger() {
		BigInteger numerator = BigInteger.valueOf(3);
		BigInteger denominator = BigInteger.valueOf(-9);
		Rational expResult = Rational.valueOf(-1, 3);
		Rational result = Rational.valueOf(numerator, denominator);
		assertEquals(expResult, result);
	}

	/**
	 * Test of valueOf method, of class Rational.
	 */
	@Test
	public void testValueOf_long() {
		Rational expResult = Rational.valueOf(1, 1);
		Rational result = Rational.valueOf(1);
		assertEquals(expResult, result);
	}

	/**
	 * Test of valueOf method, of class Rational.
	 */
	@Test
	public void testValueOf_BigInteger() {
		BigInteger number = BigInteger.valueOf(3);
		Rational expResult = Rational.valueOf(3, 1);
		Rational result = Rational.valueOf(number);
		assertEquals(expResult, result);
	}

	/**
	 * Test of add method, of class Rational.
	 */
	@Test
	public void testAdd() {
		Rational x = Rational.valueOf(5, 3);
		Rational y = Rational.valueOf(2, 9);
		Rational result = x.add(y);
		Rational expResult = Rational.valueOf(17, 9);
		assertEquals(expResult, result);
	}

	/**
	 * Test of subtract method, of class Rational.
	 */
	@Test
	public void testSubtract() {
		Rational x = Rational.valueOf(5, 3);
		Rational y = Rational.valueOf(2, 9);
		Rational result = x.subtract(y);
		Rational expResult = Rational.valueOf(13, 9);
		assertEquals(expResult, result);
	}

	/**
	 * Test of negate method, of class Rational.
	 */
	@Test
	public void testNegate() {
		assertEquals(Rational.valueOf(4, 13), Rational.valueOf(-4, 13).negate());
		assertEquals(Rational.valueOf(4, 13), Rational.valueOf(4, -13).negate());
	}

	/**
	 * Test of multiply method, of class Rational.
	 */
	@Test
	public void testMultiply() {
		Rational x = Rational.valueOf(3, 5);
		Rational y = Rational.valueOf(2, 7);
		Rational result = x.multiply(y);
		Rational expResult = Rational.valueOf(3 * 2, 5 * 7);
		assertEquals(expResult, result);
	}

	/**
	 * Test of divide method, of class Rational.
	 */
	@Test
	public void testDivide() {
		Rational x = Rational.valueOf(3, 5);
		Rational y = Rational.valueOf(2, 7);
		Rational result = x.divide(y);
		Rational expResult = Rational.valueOf(3 * 7, 5 * 2);
		assertEquals(expResult, result);
	}

	/**
	 * Test of hashCode method, of class Rational.
	 */
	@Test
	public void testHashCode() {
		int hash = Rational.valueOf(-5, -25).hashCode();
		int expected = Rational.valueOf(1, 5).hashCode();
		assertEquals(expected, hash);
	}

	/**
	 * Test of equals method, of class Rational.
	 */
	@Test
	public void testEquals() {
		assertTrue(Rational.valueOf(3, 4).equals(Rational.valueOf(6, 8)));
		assertFalse(Rational.valueOf(2, 7).equals(Rational.valueOf(2, 6)));
	}

	/**
	 * Test of compareTo method, of class Rational.
	 */
	@Test
	public void testCompareTo() {
		assertEquals(1, Rational.valueOf(5, 3).compareTo(Rational.valueOf(4, 3)));
		assertEquals(1, Rational.valueOf(5, 3).compareTo(Rational.valueOf(5, 4)));
		assertEquals(0, Rational.valueOf(4, 2).compareTo(Rational.valueOf(2)));
		assertEquals(-1, Rational.valueOf(5, 4).compareTo(Rational.valueOf(5, 3)));
		assertEquals(-1, Rational.valueOf(3, -3).compareTo(Rational.valueOf(3, 3)));
	}

	/**
	 * Test of intValue method, of class Rational.
	 */
	@Test
	public void testIntValue() {
		assertEquals(5, Rational.valueOf(10, 2).intValue());
		assertEquals(5, Rational.valueOf(11, 2).intValue());
	}

	/**
	 * Test of longValue method, of class Rational.
	 */
	@Test
	public void testLongValue() {
		assertEquals(3L, Rational.valueOf(9, 3).longValue());
		assertEquals(3L, Rational.valueOf(11, 3).longValue());
	}

	/**
	 * Test of floatValue method, of class Rational.
	 */
	@Test
	public void testFloatValue() {
		assertEquals(0.5f, Rational.valueOf(1, 2).floatValue(), 0.001f);
	}

	/**
	 * Test of doubleValue method, of class Rational.
	 */
	@Test
	public void testDoubleValue() {
		assertEquals(0.5, Rational.valueOf(1, 2).doubleValue(), 0.001);
	}

	/**
	 * Test of numerator method, of class Rational.
	 */
	@Test
	public void testNumerator() {
		final int TESTS = 100;
		Random rng = new Random(0);
		for (int i = 0; i < TESTS; i++) {
			long n = rng.nextLong();
			Rational r = Rational.valueOf(n, 1);
			assertEquals(BigInteger.valueOf(n), r.numerator());
		}
	}

	/**
	 * Test of denominator method, of class Rational.
	 */
	@Test
	public void testDenominator() {
		final int TESTS = 100;
		Random rng = new Random(0);
		for (int i = 0; i < TESTS; i++) {
			long d = rng.nextLong();
			Rational r = Rational.valueOf(1, d);
			assertEquals(BigInteger.valueOf(Math.abs(d)), r.denominator());
		}
	}

	/**
	 * Test of toString method, of class Rational.
	 */
	@Test
	public void testToString() {
		assertEquals("-1/3", Rational.valueOf(1, -3).toString());
		assertEquals("1/3", Rational.valueOf(-3, -9).toString());
	}
	
	@Test(expected=ArithmeticException.class)
	public void testCreateDivByZero() {
		Rational.valueOf(3, 0);
	}
	
	@Test(expected=ArithmeticException.class)
	public void testDivByZero() {
		Rational.ONE.divide(Rational.ZERO);
	}

}
