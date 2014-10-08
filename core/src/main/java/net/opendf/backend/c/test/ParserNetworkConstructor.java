package net.opendf.backend.c.test;

import net.opendf.backend.c.util.NetworkBuilder;
import net.opendf.ir.net.Network;

public class ParserNetworkConstructor implements NetworkConstructor {

	@Override
	public Network constructNetwork(NodeReader reader) {
		NetworkBuilder builder = new NetworkBuilder();
		addPorts(builder);
		addNodes(builder, reader);
		addConnections(builder);
		return builder.build();
	}

	private void addPorts(NetworkBuilder builder) {
		builder.addInputPort("BITS", Util.boolType());
		builder.addOutputPort("Y_AC", Util.boolType());
		builder.addOutputPort("U_AC", Util.boolType());
		builder.addOutputPort("V_AC", Util.boolType());
		builder.addOutputPort("Y_MOT", Util.boolType());
		builder.addOutputPort("WIDTH", Util.intType(16));
		builder.addOutputPort("U_MOT", Util.boolType());
		builder.addOutputPort("HEIGHT", Util.intType(16));
		builder.addOutputPort("V_MOT", Util.boolType());
		builder.addOutputPort("ROUND", Util.boolType());
		builder.addOutputPort("BTYPE_U", Util.intType(4));
		builder.addOutputPort("BTYPE_Y", Util.intType(4));
		builder.addOutputPort("BTYPE_V", Util.intType(4));
		builder.addOutputPort("ACP_Y", Util.boolType());
		builder.addOutputPort("ACP_U", Util.boolType());
		builder.addOutputPort("ACP_V", Util.boolType());
		builder.addOutputPort("MV_Y", Util.intType(9));
		builder.addOutputPort("MV_U", Util.intType(9));
		builder.addOutputPort("MV_V", Util.intType(9));
		builder.addOutputPort("B_Y", Util.intType(13));
		builder.addOutputPort("B_U", Util.intType(13));
		builder.addOutputPort("B_V", Util.intType(13));
		builder.addOutputPort("QUANT_Y", Util.intType(6));
		builder.addOutputPort("QUANT_U", Util.intType(6));
		builder.addOutputPort("QUANT_V", Util.intType(6));
	}
	
	private void addNodes(NetworkBuilder builder, NodeReader reader) {
		builder.addNode("parseheaders", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Algo_SynP"));
		builder.addNode("mvseq", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_MVSequence_LeftAndTopAndTopRight"));
		builder.addNode("blkexp", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_BlockExpand"));
		builder.addNode("mvrecon", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_MVR_MedianOfThreeLeftAndTopAndTopRight"));
		builder.addNode("splitter_BTYPE", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter_420_TYPE"));
		builder.addNode("splitter_MV", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter420MV"));
		builder.addNode("splitter_420_B", reader.fromId("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter420B"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection(null, "BITS", "parseheaders", "BITS");
		builder.addConnection("parseheaders", "MV", "mvrecon", "MVIN");
		builder.addConnection("parseheaders", "RUN", "blkexp", "RUN");
		builder.addConnection("parseheaders", "VALUE", "blkexp", "VALUE");
		builder.addConnection("parseheaders", "LAST", "blkexp", "LAST");
		builder.addConnection("mvseq", "A", "mvrecon", "A");
		builder.addConnection("blkexp", "OUT", "splitter_420_B", "B");
		builder.addConnection("mvrecon", "MV", "splitter_MV", "MV");
		builder.addConnection("splitter_MV", "MV_Y", null, "MV_Y");
		builder.addConnection("splitter_MV", "MV_U", null, "MV_U");
		builder.addConnection("splitter_MV", "MV_V", null, "MV_V");
		builder.addConnection("splitter_420_B", "B_Y", null, "B_Y");
		builder.addConnection("splitter_420_B", "B_U", null, "B_U");
		builder.addConnection("splitter_420_B", "B_V", null, "B_V");
		builder.addConnection("parseheaders", "WIDTH", null, "WIDTH");
		builder.addConnection("parseheaders", "HEIGHT", null, "HEIGHT");
		builder.addConnection("parseheaders", "WIDTH", "mvrecon", "WIDTH");
		builder.addConnection("parseheaders", "WIDTH", "mvseq", "WIDTH");
		builder.addConnection("parseheaders", "ROUND", null, "ROUND");
		builder.addConnection("parseheaders", "FCODE", "mvrecon", "FCODE");
		builder.addConnection("parseheaders", "ACCODED", "splitter_420_B", "ACCODED");
		builder.addConnection("parseheaders", "ACCODED", "splitter_BTYPE", "ACCODED");
		builder.addConnection("splitter_BTYPE", "ACCODED_Y", null, "Y_AC");
		builder.addConnection("splitter_BTYPE", "ACCODED_U", null, "U_AC");
		builder.addConnection("splitter_BTYPE", "ACCODED_V", null, "V_AC");
		builder.addConnection("parseheaders", "MOTION", "mvseq", "MOTION");
		builder.addConnection("parseheaders", "MOTION", "splitter_MV", "MOTION");
		builder.addConnection("parseheaders", "MOTION", "mvrecon", "MOTION");
		builder.addConnection("parseheaders", "MOTION", "splitter_BTYPE", "MOTION");
		builder.addConnection("splitter_BTYPE", "MOTION_Y", null, "Y_MOT");
		builder.addConnection("splitter_BTYPE", "MOTION_U", null, "U_MOT");
		builder.addConnection("splitter_BTYPE", "MOTION_V", null, "V_MOT");
		builder.addConnection("splitter_BTYPE", "ACPRED_Y", null, "ACP_Y");
		builder.addConnection("splitter_BTYPE", "ACPRED_U", null, "ACP_U");
		builder.addConnection("splitter_BTYPE", "ACPRED_V", null, "ACP_V");
		builder.addConnection("parseheaders", "ACPRED", "splitter_BTYPE", "ACPRED");
		builder.addConnection("parseheaders", "FOUR_MV", "mvseq", "FOUR_MV");
		builder.addConnection("parseheaders", "FOUR_MV", "mvrecon", "FOUR_MV");
		builder.addConnection("parseheaders", "BTYPE", "splitter_BTYPE", "BTYPE");
		builder.addConnection("splitter_BTYPE", "BTYPE_Y", null, "BTYPE_Y");
		builder.addConnection("splitter_BTYPE", "BTYPE_U", null, "BTYPE_U");
		builder.addConnection("splitter_BTYPE", "BTYPE_V", null, "BTYPE_V");
		builder.addConnection("parseheaders", "BTYPE", "mvseq", "BTYPE");
		builder.addConnection("parseheaders", "BTYPE", "splitter_420_B", "BTYPE");
		builder.addConnection("parseheaders", "BTYPE", "mvrecon", "BTYPE");
		builder.addConnection("parseheaders", "QP", "splitter_BTYPE", "QP");
		builder.addConnection("splitter_BTYPE", "QP_Y", null, "QUANT_Y");
		builder.addConnection("splitter_BTYPE", "QP_U", null, "QUANT_U");
		builder.addConnection("splitter_BTYPE", "QP_V", null, "QUANT_V");
	}
}
