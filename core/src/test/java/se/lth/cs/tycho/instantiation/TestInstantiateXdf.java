package se.lth.cs.tycho.instantiation;

import org.junit.Before;
import org.junit.Test;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.loader.SourceCodeRepository;
import se.lth.cs.tycho.messages.NullMessageReporter;

import java.nio.file.Paths;

import static org.junit.Assert.*;


public class TestInstantiateXdf {
	private DeclarationLoader loader;
	private Instantiator instantiator;
	
	@Before
	public void initialize() {
		SourceCodeRepository repo = new FileSystemXdfRepository(Paths.get("src/test/xdf"));
		loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(repo);
		instantiator = new Instantiator(loader);
	}
	
	@Test
	public void testEmpty() throws AmbiguityException {
		PortContainer instance = instantiator.instantiate(QID.of("empty"), null, QID.empty());
		assertTrue(instance instanceof Network);
		Network net = (Network) instance;
		assertTrue(net.getConnections().isEmpty());
		assertTrue(net.getNodes().isEmpty());
		assertTrue(net.getInputPorts().isEmpty());
		assertTrue(net.getOutputPorts().isEmpty());
	}

}
