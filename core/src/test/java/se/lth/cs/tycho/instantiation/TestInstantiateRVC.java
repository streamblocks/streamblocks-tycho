package se.lth.cs.tycho.instantiation;

import org.junit.Test;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortContainer;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.loader.FileSystemCalRepository;
import se.lth.cs.tycho.loader.FileSystemXdfRepository;
import se.lth.cs.tycho.messages.NullMessageReporter;
import se.lth.cs.tycho.transform.caltoam.CalActorStates;
import se.lth.cs.tycho.transform.reduction.SelectFirstReducer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TestInstantiateRVC {

	private static final QID DECODER = QID.parse("org.sc29.wg11.mpeg4.part2.sp.RVC_decoder");
	private static final Path RVC_PATH = Paths.get("../../orc-apps/RVC/src");

	@Test
	public void testInstantiateRVC() throws AmbiguityException {
		DeclarationLoader loader = new DeclarationLoader(new NullMessageReporter());
		loader.addRepository(new FileSystemXdfRepository(RVC_PATH));
		loader.addRepository(new FileSystemCalRepository(RVC_PATH));
		Instantiator instantiator = new Instantiator(loader,
				Arrays.asList(SelectFirstReducer<CalActorStates.State>::new));
		PortContainer net = instantiator.instantiate(DECODER, null, QID.empty());
		assertTrue(net instanceof Network);
		Network network = (Network) net;
	}
}
