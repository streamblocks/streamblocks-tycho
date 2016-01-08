package se.lth.cs.tycho.phases.attributes.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.function.Function;

public class CachedTest {
	@Test
	public void testIdentityCached() {
		Function<Integer, Integer> idId = Attributes.identityCached(Function.identity());
		Integer a = new Integer(1);
		Integer b = new Integer(1);
		assertSame("Wrong object", a, idId.apply(a));
		assertSame("Wrong object", b, idId.apply(b));
		assertNotSame("Wrong object", a, idId.apply(b));
		assertNotSame("Wrong object", b, idId.apply(a));
	}
	@Test
	public void testCached() {
		Function<Integer, Integer> id = Attributes.cached(Function.identity());
		Integer a = new Integer(1);
		Integer b = new Integer(1);
		assertSame("Wrong object", a, id.apply(a));
		assertSame("Wrong object", a, id.apply(b));
		assertNotSame("Wrong object", b, id.apply(b));
	}
}
