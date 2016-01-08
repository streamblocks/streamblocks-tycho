/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.lth.cs.tycho.transform.reduction.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Rational numbers with arbitrarily large numerators and denominators.
 *
 * @author gustav
 */
public final class Rational extends Number implements Comparable<Rational> {

    private final BigInteger numerator;
    private final BigInteger denominator;

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);

    /**
     * Returns numerator / denominator.
     * @param numerator the numerator
     * @param denominator the denominator
     * @return numerator / denominator
     * @throws java.lang.ArithmeticException if denominator == 0L.
     */
    public static Rational valueOf(long numerator, long denominator) {
        return normalized(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Returns numerator / denominator.
     * @param numerator the numerator
     * @param denominator the denominator
     * @return numerator / denominator
     * @throws java.lang.ArithmeticException if denominator.equals(BigInteger.ZERO)
     */
    public static Rational valueOf(BigInteger numerator, BigInteger denominator) {
        return normalized(numerator, denominator);
    }

    /**
     * Returns number / 1.
     * @param number the number
     * @return number / 1.
     */
    public static Rational valueOf(long number) {
        return new Rational(BigInteger.valueOf(number), BigInteger.ONE);
    }

    /**
     * Returns number / 1.
     * @param number the number
     * @return number / 1.
     */
    public static Rational valueOf(BigInteger number) {
        return new Rational(number, BigInteger.ONE);
    }

    private Rational(BigInteger numerator, BigInteger denominator) {
        if (denominator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("/ by zero");
        }
        this.numerator = numerator;
        this.denominator = denominator;
    }

    private static Rational normalized(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        return new Rational(numerator.divide(gcd), denominator.divide(gcd));
    }

    /**
     * a/b + c/d = (ad + bc) / bd
     */
    private static Rational add(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
        return normalized(
                a.multiply(d).add(b.multiply(c)),
                b.multiply(d)
        );
    }

    /**
     * Returns the sum of this value and the provided value.
     * @param val the value to add
     * @return this + val
     */
    public Rational add(Rational val) {
        return add(numerator, denominator, val.numerator, val.denominator);
    }

    /**
     * Returns this subtracted by the provided value.
     * @param val the value to subtract
     * @return this - val
     */
    public Rational subtract(Rational val) {
        return add(numerator, denominator, val.numerator.negate(), val.denominator);
    }

    /**
     * Returns the negation of this value.
     * @return -this
     */
    public Rational negate() {
        return new Rational(numerator.negate(), denominator);
    }

    /**
     * (a/b) * (c/d) = ac/bd
     */
    private static Rational multiply(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
        return normalized(
                a.multiply(c),
                b.multiply(d)
        );
    }

    /**
     * Returns the product of this value and the provided value.
     * @param val the value to multiply
     * @return this * val
     */
    public Rational multiply(Rational val) {
        return multiply(numerator, denominator, val.numerator, val.denominator);
    }

    /**
     * Returns this value divided by that value.
     * @param val the value to divide with
     * @return this / val
     */
    public Rational divide(Rational val) {
        return multiply(numerator, denominator, val.denominator, val.numerator);
    }

    /**
     * Return the numerator of the normalized representation of this value.
     * @return the normalized numerator
     */
    public BigInteger numerator() {
        return numerator;
    }

    /**
     * Return the denominator of the normalized representation of this value.
     * @return the normalized denominator
     */
    public BigInteger denominator() {
        return denominator;
    }

    @Override
    public int hashCode() {
        return numerator.hashCode() ^ denominator.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rational) {
            Rational val = (Rational) obj;
            return numerator.equals(val.numerator) && denominator.equals(val.denominator);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Rational val) {
        return numerator.multiply(val.denominator).compareTo(val.numerator.multiply(denominator));
    }

    @Override
    public int intValue() {
        return numerator.divide(denominator).intValue();
    }

    @Override
    public long longValue() {
        return numerator.divide(denominator).longValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), MathContext.DECIMAL64).doubleValue();
    }

    @Override
    public String toString() {
        return numerator.toString() + "/" + denominator.toString();
    }
}
