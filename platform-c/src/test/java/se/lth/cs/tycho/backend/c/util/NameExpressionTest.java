package se.lth.cs.tycho.backend.c.util;

import org.junit.Test;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.*;

import static se.lth.cs.tycho.backend.c.util.NameExpression.*;

public class NameExpressionTest {

	private final int defaultMaxNameSize = 5;
	private final int defaultMaxSeqSize = 5;
	private final int defaultNbrOfSamples = 1000;

	@Test
	public void testEquals() {
		assertEquals("NameExpression.equals is not correct",
				name("asdf"),
				name("asdf"));

		assertNotEquals("NameExpression.equals is not correct",
				name("asdf"),
				name("fdsa"));

		assertEquals("NameExpression.equals is not correct",
				seq(name("asdf"), name("fd"), seq()),
				seq(name("asdf"), name("fd"), seq()));

		assertNotEquals("NameExpression.equals is not correct",
				seq(name("asdf"), name("fd")),
				seq(name("xsdf"), name("fd")));
	}

	@Test
	public void testRandomEquals() {
		for (int i = 0; i < defaultNbrOfSamples; i++) {
			assertEquals("NameExpression.equals is not correct",
					generateRandomExpression(new Random(i), defaultMaxNameSize, defaultMaxSeqSize),
					generateRandomExpression(new Random(i), defaultMaxNameSize, defaultMaxSeqSize));
		}
	}

	@Test
	public void testEncodeName() {
		assertEquals("Name.encode is not correct", "_N3_hej", name("hej").encode());
	}

	@Test
	public void testEncodeSeq() {
		assertEquals("seq().encode() is not correct", "_S0", seq().encode());
		assertEquals("seq(seq(seq()).encode() is not correct", "_S1_S1_S0", seq(seq(seq())).encode());
	}

	@Test
	public void testEncode() {
		assertEquals("seq(name(\"a\")).encode() is not correct", "_S1_N1_a", seq(name("a")).encode());
	}

	@Test
	public void testDecodeName() {
		assertEquals("decode(name(\"a\"))", Optional.of(name("a")), decode("_N1_a"));
	}

	@Test
	public void testDecodeSeq() {
		assertEquals("decode(seq())", Optional.of(seq()), decode("_S0"));
		assertEquals("decode(seq(seq(seq())))", Optional.of(seq(seq(seq()))), decode("_S1_S1_S0"));
	}

	@Test
	public void testRandomEncodeDecode() {
		for (int i = 0; i < defaultNbrOfSamples; i++) {
			NameExpression expectedUnwrapped = generateRandomExpression(new Random(i), defaultMaxNameSize, defaultMaxSeqSize);
			Optional<NameExpression> actual = NameExpression.decode(expectedUnwrapped.encode());
			assertEquals("encode/decode did not match",
					Optional.of(expectedUnwrapped),
					actual);
		}
	}

	@Test
	public void testToString() {
		assertEquals("seq(name(\"f\"), seq(name(\"g\"), name(\"x\"))).toString()", "(f (g x))", seq(name("f"), seq(name("g"), name("x"))).toString());
	}

	private NameExpression generateRandomExpression(Random random, int maxNameSize, int maxSeqSize) {
		if (random.nextBoolean()) {
			return new NameExpression.Name(randomWord(random, maxNameSize));
		} else {
			ImmutableList.Builder builder = ImmutableList.builder();
			int n = random.nextInt(maxSeqSize + 1);
			while (n > 0) {
				builder.add(generateRandomExpression(random, maxNameSize, maxSeqSize));
				n--;
			}
			return new NameExpression.Seq(builder.build());
		}
	}

	private char randomLetter(Random random) {
		char first;
		char last;
		if (random.nextBoolean()) {
			first = 'A';
			last = 'Z';
		} else {
			first = 'a';
			last = 'z';
		}
		return (char) (random.nextInt(last - first + 1) + first);
	}

	private String randomWord(Random random, int maxWordSize) {
		int n = random.nextInt(maxWordSize + 1);
		StringBuilder builder = new StringBuilder(n);
		while (n > 0) {
			builder.append(randomLetter(random));
			n--;
		}
		return builder.toString();
	}

}