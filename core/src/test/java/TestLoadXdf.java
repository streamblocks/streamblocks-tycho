import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.xdf.XDF;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.MessageWriter;


public class TestLoadXdf {

	@Test
	public void testLoadXdf() {
		DeclarationLoader loader = new DeclarationLoader(new MessageWriter());
		loader.addRepository(new FileSystemXdfRepository(Paths.get("src/test/xdf")));
		GlobalEntityDecl empty = loader.loadEntity(QID.of("empty"), null);
		assertNotNull(empty);
		assertTrue(empty.getEntity() instanceof XDF);
		GlobalEntityDecl x = loader.loadEntity(QID.parse("a.b.c.x"), null);
		assertNotNull(x);
		assertTrue(x.getEntity() instanceof XDF);
	}

	@Test
	public void testLoadNoXdf() {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(new FileSystemXdfRepository(Paths.get("src/test/xdf")));
		GlobalEntityDecl noDecl = loader.loadEntity(QID.parse("does.not.exist"), null);
		assertNull(noDecl);
	}
}
