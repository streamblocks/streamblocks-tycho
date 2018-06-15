package se.lth.cs.tycho.backend.c.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringSourceTest {
	@Test
	public void assertEOF() throws Exception {
		new StringSource("").assertEOF();
	}

	@Test(expected = StringSource.SourceException.class)
	public void assertEOFException() throws Exception {
		new StringSource("a").assertEOF();
	}

	@Test
	public void getChar() throws Exception {
		StringSource src = new StringSource("asdf");
		assertEquals("StringSource.getChar", 'a', src.getChar());
		assertEquals("StringSource.getChar", 's', src.getChar());
		assertEquals("StringSource.getChar", 'd', src.getChar());
		assertEquals("StringSource.getChar", 'f', src.getChar());
		src.assertEOF();
	}

	@Test
	public void take() throws Exception {
		StringSource src = new StringSource("asdf");
		assertEquals("StringSource.take", "as", src.take(2));
		assertEquals("StringSource.take", "df", src.take(2));
		src.assertEOF();
	}

	@Test
	public void consumeChar() throws Exception {
		StringSource src = new StringSource("asdf");
		src.consumeChar('a');
		src.consumeChar('s');
		src.consumeChar('d');
		src.consumeChar('f');
		src.assertEOF();
	}

	@Test(expected = StringSource.SourceException.class)
	public void consumeCharFailure() throws Exception {
		new StringSource("sdf").consumeChar('a');
	}
	@Test
	public void readNumber() throws Exception {
		StringSource src = new StringSource("123_456");
		assertEquals("StringSource.readNumber", 123, src.readNumber());
		src.consumeChar('_');
		assertEquals("StringSource.readNumber", 456, src.readNumber());
		src.assertEOF();
	}

	@Test
	public void takeWhile() throws Exception {
		StringSource src = new StringSource("asdf fdsa");
		assertEquals("StringSource.takeWhile", "asdf", src.takeWhile(c -> c != ' '));
		assertEquals("Should be space", ' ', src.getChar());
		assertEquals("StringSource.takeWhile", "fdsa", src.takeWhile(c -> c != ' '));
	}

}