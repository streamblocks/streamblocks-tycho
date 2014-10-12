package se.lth.cs.tycho.instantiation;
import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instantiation.net.NetworkInstantiator;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.loader.SourceCodeRepository;
import se.lth.cs.tycho.messages.NullMessageReporter;


public class TestInstantiateXdf {
	private DeclarationLoader loader;
	private NetworkInstantiator instantiator;
	
	@Before
	public void initialize() {
		SourceCodeRepository repo = new FileSystemXdfRepository(Paths.get("src/test/xdf"));
		loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(repo);
		instantiator = new NetworkInstantiator(loader);
	}
	
	@Test
	public void testEmpty() {
		GlobalEntityDecl decl = loader.loadEntity(QID.of("empty"), null);
		Instance instance = instantiator.instantiate(decl, ImmutableList.empty(), ImmutableList.empty());
		assertTrue(instance instanceof Network);
		Network net = (Network) instance;
		assertTrue(net.getConnections().isEmpty());
		assertTrue(net.getNodes().isEmpty());
		assertTrue(net.getInputPorts().isEmpty());
		assertTrue(net.getOutputPorts().isEmpty());
	}

}
