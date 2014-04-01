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


public class TestMotion8x8Comp {
	private ActorToActorMachine translator = new ActorToActorMachine(ImmutableList.of(
			PrioritizeCallInstructions.<ActorStates.State> getFactory()));

	private final String BASE_PATH;

	public TestMotion8x8Comp(String basePath) {
		BASE_PATH = basePath;
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

	private TypeExpr uint(int size) {
		Expression s = new ExprLiteral(ExprLiteral.Kind.Integer, Integer.toString(size));
		return new TypeExpr("uint", null, ImmutableList.of(ImmutableEntry.of("size", s)));
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
		builder.addInputPort("QP", int_t(6));
		builder.addInputPort("WIDTH", int_t(16));
		builder.addInputPort("ACCODED", bool_t());
		builder.addInputPort("BTYPE", int_t(4));
		builder.addInputPort("ACPRED", bool_t());
		builder.addInputPort("QFS", int_t(13));
		builder.addOutputPort("f", bool_t());
	}

	private void addNodes(NetworkBuilder builder) {
		builder.addNode("DCsplit", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
		builder.addNode("addressing", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_8x8"));
		builder.addNode("invpred", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_CHROMA_8x8"));
		builder.addNode("IS", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
		builder.addNode("IAP", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_8x8"));
		builder.addNode("IQ", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("idct2d", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection(null, "QFS", "DCsplit", "IN");
		builder.addConnection("DCsplit", "DC", "invpred", "QFS_DC", bufferSize(1));
		builder.addConnection("DCsplit", "AC", "IS", "QFS_AC", bufferSize(1));
		builder.addConnection("IS", "PQF_AC", "IAP", "PQF_AC", bufferSize(1));
		builder.addConnection("IAP", "QF_AC", "IQ", "AC", bufferSize(1));
		builder.addConnection("IQ", "OUT", "idct2d", "IN", bufferSize(64));
		builder.addConnection("idct2d", "OUT", null, "f");
		builder.addConnection("invpred", "SIGNED", "idct2d", "SIGNED", bufferSize(1));
		builder.addConnection("invpred", "QUANT", "IQ", "QP", bufferSize(1));
		builder.addConnection("invpred", "QF_DC", "IQ", "DC", bufferSize(1));
		builder.addConnection("invpred", "PTR", "IAP", "PTR", bufferSize(1));
		builder.addConnection("invpred", "AC_PRED_DIR", "IAP", "AC_PRED_DIR", bufferSize(1));
		builder.addConnection("invpred", "AC_PRED_DIR", "IS", "AC_PRED_DIR", bufferSize(1));
		builder.addConnection(null, "QP", "invpred", "QP");
		builder.addConnection("invpred", "QUANT", "IAP", "QP", bufferSize(1));
		builder.addConnection("invpred", "PREV_QUANT", "IAP", "PREV_QP", bufferSize(1));
		builder.addConnection(null, "WIDTH", "invpred", "WIDTH");
		builder.addConnection(null, "WIDTH", "addressing", "WIDTH");
		builder.addConnection(null, "ACCODED", "invpred", "AC_CODED");
		builder.addConnection(null, "ACPRED", "invpred", "AC_PRED");
		builder.addConnection(null, "BTYPE", "invpred", "BTYPE");
		builder.addConnection(null, "BTYPE", "addressing", "BTYPE");
		builder.addConnection("addressing", "A", "invpred", "A", bufferSize(1));
		builder.addConnection("addressing", "B", "invpred", "B", bufferSize(1));
		builder.addConnection("addressing", "C", "invpred", "C", bufferSize(1));
	}

	public static void main(String[] args) throws FileNotFoundException {
		TestMotion8x8Comp test = new TestMotion8x8Comp("../orc-apps/RVC/src/");
		Network network = test.network();
		Network composition = new Composer().composeNetwork(network, "composition");
		PrintWriter writer = new PrintWriter("motion_comp.c");
		Backend.generateCode(composition, writer);
	}

}
