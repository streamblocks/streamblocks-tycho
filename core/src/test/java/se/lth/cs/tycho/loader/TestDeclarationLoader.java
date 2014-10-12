package se.lth.cs.tycho.loader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.messages.NullMessageReporter;


public class TestDeclarationLoader {
	@Test
	public void testFindInEmpty() {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		assertNull(loader.loadEntity(QID.parse("a.b.c"), null));
		assertNull(loader.loadEntity(QID.parse("a.b.c"), null));
	}
	
	@Test
	public void testLoadPublicDecl() {
		StringRepository repo = new StringRepository();
		repo.add("namespace a.b: namespace c: public int x = 2; public int y = 4; end end");
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(repo);
		GlobalVarDecl x = loader.loadVar(QID.parse("a.b.c.x"), null);
		assertNotNull(x);
		assertEquals("x", x.getName());
		GlobalVarDecl y = loader.loadVar(QID.parse("a.b.c.y"), null);
		assertNotNull(y);
		assertEquals("y", y.getName());
	}
	
	@Test
	public void testGetLocationAndQID() {
		StringRepository repo = new StringRepository();
		repo.add("namespace a: namespace b: int x = 2; end end");
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(repo);
		GlobalVarDecl x = loader.loadVar(QID.parse("a.b.x"), null);
		assertNotNull(x);
		assertEquals(QID.parse("a.b.x"), loader.getQID(x));
		NamespaceDecl b = loader.getLocation(x);
		assertNotNull(b);
		assertEquals(QID.of("b"), b.getQID());
		assertEquals(QID.parse("a.b"), loader.getQID(b));
		NamespaceDecl a = loader.getLocation(b);
		assertNotNull(a);
		assertEquals(QID.of("a"), a.getQID());
		assertEquals(QID.parse("a"), loader.getQID(a));
		NamespaceDecl root = loader.getLocation(a);
		while(root != null) {
			assertEquals(QID.empty(), root.getQID());
			assertEquals(QID.empty(), loader.getQID(root));
			root = loader.getLocation(root);
		}
	}
}
