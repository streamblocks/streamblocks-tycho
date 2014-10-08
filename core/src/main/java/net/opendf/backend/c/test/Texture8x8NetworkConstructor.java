package net.opendf.backend.c.test;

import net.opendf.backend.c.util.NetworkBuilder;
import net.opendf.ir.net.Network;

public class Texture8x8NetworkConstructor implements NetworkConstructor {

	@Override
	public Network constructNetwork(NodeReader reader) {
		NetworkBuilder builder = new NetworkBuilder();
		addPorts(builder);
		addNodes(builder, reader);
		addConnections(builder);
		return builder.build();
	}

	private void addPorts(NetworkBuilder builder) {
		builder.addInputPort("QP", Util.intType(6));
		builder.addInputPort("WIDTH", Util.intType(16));
		builder.addInputPort("ACCODED", Util.boolType());
		builder.addInputPort("BTYPE", Util.intType(4));
		builder.addInputPort("ACPRED", Util.boolType());
		builder.addInputPort("QFS", Util.intType(13));
		builder.addOutputPort("f", Util.boolType());
	}

	private void addNodes(NetworkBuilder builder, NodeReader reader) {
		builder.addNode("DCsplit", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
		builder.addNode("addressing", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_8x8"));
		builder.addNode("invpred", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_CHROMA_8x8"));
		builder.addNode("IS", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
		builder.addNode("IAP", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_8x8"));
		builder.addNode("IQ", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("idct2d", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection(null, "QFS", "DCsplit", "IN");
		builder.addConnection("DCsplit", "DC", "invpred", "QFS_DC");
		builder.addConnection("DCsplit", "AC", "IS", "QFS_AC");
		builder.addConnection("IS", "PQF_AC", "IAP", "PQF_AC");
		builder.addConnection("IAP", "QF_AC", "IQ", "AC");
		builder.addConnection("IQ", "OUT", "idct2d", "IN");
		builder.addConnection("idct2d", "OUT", null, "f");
		builder.addConnection("invpred", "SIGNED", "idct2d", "SIGNED");
		builder.addConnection("invpred", "QUANT", "IQ", "QP");
		builder.addConnection("invpred", "QF_DC", "IQ", "DC");
		builder.addConnection("invpred", "PTR", "IAP", "PTR");
		builder.addConnection("invpred", "AC_PRED_DIR", "IAP", "AC_PRED_DIR");
		builder.addConnection("invpred", "AC_PRED_DIR", "IS", "AC_PRED_DIR");
		builder.addConnection(null, "QP", "invpred", "QP");
		builder.addConnection("invpred", "QUANT", "IAP", "QP");
		builder.addConnection("invpred", "PREV_QUANT", "IAP", "PREV_QP");
		builder.addConnection(null, "WIDTH", "invpred", "WIDTH");
		builder.addConnection(null, "WIDTH", "addressing", "WIDTH");
		builder.addConnection(null, "ACCODED", "invpred", "AC_CODED");
		builder.addConnection(null, "ACPRED", "invpred", "AC_PRED");
		builder.addConnection(null, "BTYPE", "invpred", "BTYPE");
		builder.addConnection(null, "BTYPE", "addressing", "BTYPE");
		builder.addConnection("addressing", "A", "invpred", "A");
		builder.addConnection("addressing", "B", "invpred", "B");
		builder.addConnection("addressing", "C", "invpred", "C");
	}

}
