package se.lth.cs.tycho.loader;
import static org.junit.Assert.*;

import org.junit.Test;

import se.lth.cs.tycho.ir.QID;

public class TestQID {
	@Test
	public void testToString() {
		assertEquals("abc.def.ghi", QID.of("abc", "def", "ghi").toString());
	}

	@Test
	public void testParse() {
		QID parse = QID.parse("abc.def.ghi");
		QID built = QID.of("abc", "def", "ghi");
		assertEquals(built, parse);
	}

	@Test
	public void testParseEmpty() {
		QID empty = QID.parse("");
		assertEquals(0, empty.getNameCount());
		assertEquals(QID.empty(), empty);
	}

	@Test
	public void testOfEmpty() {
		QID empty = QID.of();
		assertEquals(0, empty.getNameCount());
		assertEquals(QID.empty(), empty);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfWithDot() {
		QID.of("abc.def");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfWithEmptyString() {
		QID.of("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseWithEmptyPart() {
		QID.parse("abc..def");
	}

	@Test
	public void testGetFirst() {
		assertEquals(QID.of("abc"), QID.parse("abc.def.ghi").getFirst());
		assertEquals(QID.of("abc"), QID.parse("abc.def").getFirst());
		assertEquals(QID.of("abc"), QID.parse("abc").getFirst());
		assertNull(QID.empty().getFirst());
	}

	@Test
	public void testGetLast() {
		assertEquals(QID.of("ghi"), QID.parse("abc.def.ghi").getLast());
		assertEquals(QID.of("def"), QID.parse("abc.def").getLast());
		assertEquals(QID.of("abc"), QID.parse("abc").getLast());
		assertNull(QID.empty().getLast());
	}

	@Test
	public void testNameCount() {
		assertEquals(0, QID.empty().getNameCount());
		assertEquals(1, QID.of("abc").getNameCount());
		assertEquals(2, QID.of("abc", "def").getNameCount());
	}

	@Test
	public void testGetButFirst() {
		assertEquals(QID.parse("def.ghi"), QID.parse("abc.def.ghi").getButFirst());
		assertEquals(QID.parse("def"), QID.parse("abc.def").getButFirst());
		assertEquals(QID.empty(), QID.parse("abc").getButFirst());
		assertNull(QID.empty().getButFirst());
	}

	@Test
	public void testGetButLast() {
		assertEquals(QID.parse("abc.def"), QID.parse("abc.def.ghi").getButLast());
		assertEquals(QID.parse("abc"), QID.parse("abc.def").getButLast());
		assertEquals(QID.empty(), QID.parse("abc").getButLast());
		assertNull(QID.empty().getButLast());
	}

	@Test
	public void testGetName() {
		QID qid = QID.parse("abc.def.ghi");
		assertEquals(QID.of("abc"), qid.getName(0));
		assertEquals(QID.of("def"), qid.getName(1));
		assertEquals(QID.of("ghi"), qid.getName(2));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getGetNameNegativeIndex() {
		QID.parse("abc.def.ghi").getName(-1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getGetNameTooHighIndex() {
		QID.parse("abc.def.ghi").getName(3);
	}

	@Test
	public void testChangeInputArray() {
		String[] args = new String[] { "abc", "def", "ghi" };
		QID qid = QID.of(args);
		args[0] = "x";
		assertEquals(QID.parse("abc.def.ghi"), qid);
	}

	@Test(expected = NullPointerException.class)
	public void testEqualsNullThrowsException() {
		QID.parse("abc.def").equals(null);
	}

	@Test
	public void testConcat() {
		assertEquals(QID.empty(), QID.empty().concat(QID.empty()));
		assertEquals(QID.of("abc"), QID.empty().concat(QID.of("abc")));
		assertEquals(QID.of("abc"), QID.of("abc").concat(QID.empty()));
		assertEquals(QID.parse("abc.def.ghi.jkl"), QID.parse("abc.def").concat(QID.parse("ghi.jkl")));
	}

	@Test
	public void testPrefix() {
		QID qid = QID.parse("abc.def");
		assertTrue(qid.isPrefixOf(QID.parse("abc.def")));
		assertTrue(qid.isPrefixOf(QID.parse("abc.def.ghi")));
		assertFalse(qid.isPrefixOf(QID.parse("abc.defg")));
		assertTrue(QID.empty().isPrefixOf(qid));
		assertTrue(QID.empty().isPrefixOf(QID.empty()));
		assertFalse(qid.isPrefixOf(QID.parse("abc")));
	}
	
	@Test
	public void testGetWithoutPrefix() {
		QID qid = QID.parse("abc.def.ghi");
		assertNull(qid.getWithoutPrefix(QID.of("xyz")));
		assertNull(QID.parse("abc.def").getWithoutPrefix(qid));
		assertEquals(qid.getWithoutPrefix(qid), QID.empty());
		assertEquals(qid.getWithoutPrefix(QID.empty()), qid);
		assertEquals(qid.getWithoutPrefix(qid.getFirst()), qid.getButFirst());
		assertEquals(qid.getWithoutPrefix(qid.getButLast()), qid.getLast());
	}

	@Test
	public void testNotModified() {
		QID qid = QID.parse("abc.def.ghi");
		qid.getFirst();
		qid.getLast();
		qid.getButFirst();
		qid.getButLast();
		qid.getName(2);
		qid.getNameCount();
		qid.concat(QID.of("jkl"));
		qid.isPrefixOf(QID.of("zyx"));
		qid.parts();
		qid.hashCode();
		qid.equals(QID.of("xyz"));
		assertEquals(QID.parse("abc.def.ghi"), qid);
	}

}
