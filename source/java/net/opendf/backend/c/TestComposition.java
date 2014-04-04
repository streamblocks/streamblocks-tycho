package net.opendf.backend.c;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import net.opendf.backend.c.util.NetworkBuilder;
import net.opendf.errorhandling.ErrorModule;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.net.ToolValueAttribute;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.lth.CalParser;
import net.opendf.transform.caltoam.ActorStates;
import net.opendf.transform.caltoam.ActorToActorMachine;
import net.opendf.transform.compose.Composer;
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.operators.ActorOpTransformer;
import net.opendf.transform.outcond.OutputConditionAdder;
import net.opendf.transform.siam.PickFirstInstruction;
import net.opendf.transform.util.StateHandler;


public class TestComposition {
	private ActorToActorMachine translator = new ActorToActorMachine() {
		@Override
		protected StateHandler<ActorStates.State> getStateHandler(StateHandler<ActorStates.State> stateHandler) {
			stateHandler = new PrioritizeCallInstructions<>(stateHandler);
			return stateHandler;
		}
	};

	private final String BASE_PATH;

	public TestComposition(String basePath) {
		BASE_PATH = basePath;
	}

	private ActorMachine actorMachine(String actorName) {
		String fileName = BASE_PATH + actorName.replace('.', '/') + ".cal";
		File file = new File(fileName);
		CalParser parser = new CalParser();
		Actor actor = parser.parse(file, null, null);
		ErrorModule errors = parser.getErrorModule();
		if (errors.hasError()) {
			errors.printErrors();
			return null;
		}
		actor = ActorOpTransformer.transformActor(actor, null);
		ActorMachine actorMachine = translator.translate(actor);
		actorMachine = OutputConditionAdder.addOutputConditions(actorMachine);
		actorMachine = PickFirstInstruction.transform(actorMachine);
		return actorMachine;
	}

	private TypeExpr uint_t(int size) {
		return sizedType("uint", size);
	}
	
	private TypeExpr int_t(int size) {
		return sizedType("int", size);
	}
	
	private TypeExpr bool_t() {
		return new TypeExpr("bool", null, null);
	}
	
	private TypeExpr sizedType(String name, int size) {
		Expression s = new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(size));
		return new TypeExpr(name, null, ImmutableList.of(ImmutableEntry.of("size", s)));
	}

	private ImmutableList<ToolAttribute> bufferSize(int size) {
		Expression s = new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(size));
		return ImmutableList.<ToolAttribute> of(new ToolValueAttribute("buffer_size", s));
	}

	private Network network() {
		NetworkBuilder builder = new NetworkBuilder();
		addPorts(builder);
		addNodes(builder);
		addConnections(builder);
		return builder.build();
	}

	private void addPorts(NetworkBuilder builder) {
		builder.addInputPort("QF_AC", int_t(16));
		builder.addInputPort("QF_DC", int_t(16));
		builder.addInputPort("QUANT", int_t(8));
		builder.addInputPort("SIGNED", bool_t());
		builder.addOutputPort("OUT", uint_t(8));
	}

	private void addNodes(NetworkBuilder builder) {
		builder.addNode("decoder_texture_U_IQ", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("decoder_texture_U_idct2d", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection(null, "QF_AC", "decoder_texture_U_IQ", "AC");
		builder.addConnection("decoder_texture_U_IQ", "OUT", "decoder_texture_U_idct2d", "IN", bufferSize(64));
		builder.addConnection(null, "SIGNED", "decoder_texture_U_idct2d", "SIGNED");
		builder.addConnection(null, "QUANT", "decoder_texture_U_IQ", "QP");
		builder.addConnection(null, "QF_DC", "decoder_texture_U_IQ", "DC");
		builder.addConnection("decoder_texture_U_idct2d", "OUT", null, "OUT");
	}

	public static void main(String[] args) throws FileNotFoundException {
		TestComposition test = new TestComposition("../orc-apps/RVC/src/");
		Network network = test.network();
		Network composition = new Composer().composeNetwork(network, "composition");
		PrintWriter writer = new PrintWriter("composition.c");
		Backend.generateCode(composition, writer);
	}

}
