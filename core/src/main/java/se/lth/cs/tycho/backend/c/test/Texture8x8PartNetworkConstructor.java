package se.lth.cs.tycho.backend.c.test;

import se.lth.cs.tycho.backend.c.util.NetworkBuilder;
import se.lth.cs.tycho.ir.net.Network;

public class Texture8x8PartNetworkConstructor implements NetworkConstructor {

	@Override
	public Network constructNetwork(NodeReader reader) {
		NetworkBuilder builder = new NetworkBuilder();
		addPorts(builder);
		addNodes(builder, reader);
		addConnections(builder);
		return builder.build();
	}

	private void addPorts(NetworkBuilder builder) {
		builder.addInputPort("AC", Util.intType(13));
		builder.addInputPort("QP", Util.intType(6));
		builder.addInputPort("DC", Util.intType(13));
		builder.addInputPort("SIGNED", Util.boolType());
		builder.addOutputPort("f", Util.boolType());
	}

	private void addNodes(NetworkBuilder builder, NodeReader reader) {
//		builder.addNode("DCsplit", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
//		builder.addNode("addressing", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_8x8"));
//		builder.addNode("invpred", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_CHROMA_8x8"));
//		builder.addNode("IS", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
//		builder.addNode("IAP", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_8x8"));
		builder.addNode("IQ", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("idct2d", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
	}

	private void addConnections(NetworkBuilder builder) {
//		builder.addConnection(null, "QFS", "DCsplit", "IN");
//		builder.addConnection("DCsplit", "DC", "invpred", "QFS_DC", Util.bufferSize(1));
//		builder.addConnection("DCsplit", "AC", "IS", "QFS_AC", Util.bufferSize(63));
//		builder.addConnection("IS", "PQF_AC", "IAP", "PQF_AC", Util.bufferSize(1));
//		builder.addConnection("IAP", "QF_AC", "IQ", "AC", Util.bufferSize(63));
		builder.addConnection("IQ", "OUT", "idct2d", "IN", Util.bufferSize(64));
//		builder.addConnection("idct2d", "OUT", null, "f");
//		builder.addConnection("invpred", "SIGNED", "idct2d", "SIGNED", Util.bufferSize(2));
//		builder.addConnection("invpred", "QUANT", "IQ", "QP", Util.bufferSize(1));
//		builder.addConnection("invpred", "QF_DC", "IQ", "DC", Util.bufferSize(1));
//		builder.addConnection("invpred", "PTR", "IAP", "PTR", Util.bufferSize(1));
//		builder.addConnection("invpred", "AC_PRED_DIR", "IAP", "AC_PRED_DIR", Util.bufferSize(1));
//		builder.addConnection("invpred", "AC_PRED_DIR", "IS", "AC_PRED_DIR", Util.bufferSize(1));
//		builder.addConnection(null, "QP", "invpred", "QP");
//		builder.addConnection("invpred", "QUANT", "IAP", "QP", Util.bufferSize(1));
//		builder.addConnection("invpred", "PREV_QUANT", "IAP", "PREV_QP", Util.bufferSize(1));
//		builder.addConnection(null, "WIDTH", "invpred", "WIDTH");
//		builder.addConnection(null, "WIDTH", "addressing", "WIDTH");
//		builder.addConnection(null, "ACCODED", "invpred", "AC_CODED");
//		builder.addConnection(null, "ACPRED", "invpred", "AC_PRED");
//		builder.addConnection(null, "BTYPE", "invpred", "BTYPE");
//		builder.addConnection(null, "BTYPE", "addressing", "BTYPE");
//		builder.addConnection("addressing", "A", "invpred", "A", Util.bufferSize(1));
//		builder.addConnection("addressing", "B", "invpred", "B", Util.bufferSize(1));
//		builder.addConnection("addressing", "C", "invpred", "C", Util.bufferSize(1));

		builder.addConnection(null, "AC", "IQ", "AC");
		builder.addConnection(null, "QP", "IQ", "QP");
		builder.addConnection(null, "DC", "IQ", "DC");
		builder.addConnection(null, "SIGNED", "idct2d", "SIGNED");
		builder.addConnection("idct2d", "OUT", null, "f");
	}

}
