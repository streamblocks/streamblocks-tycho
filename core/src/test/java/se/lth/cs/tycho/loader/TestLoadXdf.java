package se.lth.cs.tycho.loader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.MessageWriter;
import se.lth.cs.tycho.messages.NullMessageReporter;


public class TestLoadXdf {

	@Test
	public void testLoadXdf() throws AmbiguityException {
		DeclarationLoader loader = new DeclarationLoader(new MessageWriter());
		loader.addRepository(new FileSystemXdfRepository(Paths.get("src/test/xdf")));
		EntityDecl empty = loader.loadEntity(QID.of("empty"), null);
		assertNotNull(empty);
		assertTrue(empty.getEntity() instanceof XDFNetwork);
		EntityDecl x = loader.loadEntity(QID.parse("a.b.c.x"), null);
		assertNotNull(x);
		assertTrue(x.getEntity() instanceof XDFNetwork);
	}

	@Test
	public void testLoadNoXdf() throws AmbiguityException {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(new FileSystemXdfRepository(Paths.get("src/test/xdf")));
		EntityDecl noDecl = loader.loadEntity(QID.parse("does.not.exist"), null);
		assertNull(noDecl);
	}
}
