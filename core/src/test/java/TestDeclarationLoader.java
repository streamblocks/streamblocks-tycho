import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.loader.DeclarationLoader;


public class TestDeclarationLoader {
	@Test
	public void testFindInEmpty() {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageListener());
		assertNull(loader.loadEntity(QID.parse("a.b.c"), null));
		assertNull(loader.loadEntity(QID.parse("a.b.c"), null));
	}
	
	@Test
	public void testFindExistingDecl() {
		StringRepository repo = new StringRepository();
		repo.add("namespace a.b: namespace c: public int x = 2; public int y = 4; end end");
		DeclarationLoader loader = new DeclarationLoader(new NullMessageListener());
		loader.addRepository(repo);
		GlobalVarDecl x = loader.loadVar(QID.parse("a.b.c.x"), null);
		assertNotNull(x);
		assertEquals("x", x.getName());
		GlobalVarDecl y = loader.loadVar(QID.parse("a.b.c.y"), null);
		assertNotNull(y);
		assertEquals("y", y.getName());
	}

}
