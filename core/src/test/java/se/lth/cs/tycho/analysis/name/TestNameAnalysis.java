package se.lth.cs.tycho.analysis.name;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javarag.AttributeEvaluator;
import javarag.AttributeRegister;
import javarag.impl.reg.BasicAttributeRegister;

import org.junit.Test;

import se.lth.cs.tycho.analysis.util.IRNodeTraverserWithTreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRoot;
import se.lth.cs.tycho.analysis.util.TreeRootModule;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemCalRepository;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.Message;
import se.lth.cs.tycho.messages.NullMessageReporter;

public class TestNameAnalysis {

	private static final QID PARSER = QID.parse("org.sc29.wg11.mpeg4.part2.sp.parser.Algo_SynP");
	private static final Path RVC_PATH = Paths.get("../../orc-apps/RVC/src");

	@Test
	public void testNameAnalysisOnRVCParser() throws AmbiguityException {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(new FileSystemXdfRepository(RVC_PATH));
		loader.addRepository(new FileSystemCalRepository(RVC_PATH));
		EntityDecl decl = loader.loadEntity(PARSER, null);
		TreeRoot root = new TreeRoot(loader, decl);
		AttributeRegister register = new BasicAttributeRegister();
		register.register(TreeRootModule.class, NameAnalysis.class, Imports.class, NamespaceDecls.class);
		AttributeEvaluator eval = register.getEvaluator(root, new IRNodeTraverserWithTreeRoot());
		Set<Message> errors = eval.evaluate("nameErrors", root);
		if (!errors.isEmpty()) {
			fail(errors.iterator().next().toString());
		}
	}
	
}
