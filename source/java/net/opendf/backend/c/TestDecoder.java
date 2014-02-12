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
import net.opendf.transform.filter.PrioritizeCallInstructions;
import net.opendf.transform.filter.SelectRandomInstruction;
import net.opendf.transform.operators.ActorOpTransformer;
import net.opendf.transform.outcond.OutputConditionAdder;


public class TestDecoder {
	private ActorToActorMachine translator = new ActorToActorMachine(ImmutableList.of(
			PrioritizeCallInstructions.<ActorStates.State> getFactory(),
			SelectRandomInstruction.<ActorStates.State> getFactory()));

	private final String BASE_PATH;

	public TestDecoder(String basePath) {
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
		return OutputConditionAdder.addOutputConditions(actorMachine);
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
		builder.addInputPort("INPUT", uint(8));
		builder.addOutputPort("OUTPUT", uint(8));
	}

	private void addNodes(NetworkBuilder builder) {
		builder.addNode("decoder_serialize", actorMachine("org.sc29.wg11.common.Algo_Byte2bit"));
		builder.addNode("decoder_Merger420", actorMachine("org.sc29.wg11.common.Mgnt_Merger420"));
		builder.addNode("decoder_parser_parseheaders", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Algo_SynP"));
		builder.addNode("decoder_parser_mvseq", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_MVSequence_LeftAndTopAndTopRight"));
		builder.addNode("decoder_parser_blkexp", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_BlockExpand"));
		builder.addNode("decoder_parser_mvrecon", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_MVR_MedianOfThreeLeftAndTopAndTopRight"));
		builder.addNode("decoder_parser_splitter_BTYPE", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter_420_TYPE"));
		builder.addNode("decoder_parser_splitter_MV", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter420MV"));
		builder.addNode("decoder_parser_splitter_420_B", actorMachine("org.sc29.wg11.mpeg4.part2.sp.parser.Mgnt_Splitter420B"));
		builder.addNode("decoder_texture_Y_DCsplit", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
		builder.addNode("decoder_texture_Y_IS", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
		builder.addNode("decoder_texture_Y_IAP", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_16x16"));
		builder.addNode("decoder_texture_Y_IQ", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("decoder_texture_Y_idct2d", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
		builder.addNode("decoder_texture_Y_DCRecontruction_addressing", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_16x16"));
		builder.addNode("decoder_texture_Y_DCRecontruction_invpred", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_LUMA_16x16"));
		builder.addNode("decoder_texture_U_DCsplit", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
		builder.addNode("decoder_texture_U_IS", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
		builder.addNode("decoder_texture_U_IAP", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_8x8"));
		builder.addNode("decoder_texture_U_IQ", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("decoder_texture_U_idct2d", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
		builder.addNode("decoder_texture_U_DCRecontruction_addressing", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_8x8"));
		builder.addNode("decoder_texture_U_DCRecontruction_invpred", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_CHROMA_8x8"));
		builder.addNode("decoder_texture_V_DCsplit", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Mgnt_DCSplit"));
		builder.addNode("decoder_texture_V_IS", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IS_ZigzagOrAlternateHorizontalVertical_8x8"));
		builder.addNode("decoder_texture_V_IAP", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IAP_AdaptiveHorizontalOrVerticalPred_8x8"));
		builder.addNode("decoder_texture_V_IQ", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IQ_QSAbdQmatrixMp4vOrH263Scaler"));
		builder.addNode("decoder_texture_V_idct2d", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.Algo_IDCT2D_ISOIEC_23002_1"));
		builder.addNode("decoder_texture_V_DCRecontruction_addressing", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRAddr_ThreeLeftTop_8x8"));
		builder.addNode("decoder_texture_V_DCRecontruction_invpred", actorMachine("org.sc29.wg11.mpeg4.part2.sp.texture.dc_reconstruction.Algo_DCRInvPred_CHROMA_8x8"));
		builder.addNode("decoder_motion_Y_interpolation", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_Interp_HalfpelBilinearRoundingControl"));
		builder.addNode("decoder_motion_Y_add", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_PictureReconstruction_Saturation"));
		builder.addNode("decoder_motion_Y_FrameBuff", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Mgnt_FB_w_Address_16X16"));
		builder.addNode("decoder_motion_U_interpolation", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_Interp_HalfpelBilinearRoundingControl"));
		builder.addNode("decoder_motion_U_add", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_PictureReconstruction_Saturation"));
		builder.addNode("decoder_motion_U_FrameBuff", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Mgnt_FB_w_Address_8X8"));
		builder.addNode("decoder_motion_V_interpolation", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_Interp_HalfpelBilinearRoundingControl"));
		builder.addNode("decoder_motion_V_add", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Algo_PictureReconstruction_Saturation"));
		builder.addNode("decoder_motion_V_FrameBuff", actorMachine("org.sc29.wg11.mpeg4.part2.sp.motion.Mgnt_FB_w_Address_8X8"));
	}

	private void addConnections(NetworkBuilder builder) {
		builder.addConnection("decoder_parser_parseheaders", "MV", "decoder_parser_mvrecon", "MVIN");
		builder.addConnection("decoder_parser_parseheaders", "RUN", "decoder_parser_blkexp", "RUN");
		builder.addConnection("decoder_parser_parseheaders", "VALUE", "decoder_parser_blkexp", "VALUE");
		builder.addConnection("decoder_parser_parseheaders", "LAST", "decoder_parser_blkexp", "LAST");
		builder.addConnection("decoder_parser_mvseq", "A", "decoder_parser_mvrecon", "A");
		builder.addConnection("decoder_parser_blkexp", "OUT", "decoder_parser_splitter_420_B", "B");
		builder.addConnection("decoder_parser_mvrecon", "MV", "decoder_parser_splitter_MV", "MV");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_parser_mvrecon", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_parser_mvseq", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "FCODE", "decoder_parser_mvrecon", "FCODE");
		builder.addConnection("decoder_parser_parseheaders", "ACCODED", "decoder_parser_splitter_420_B", "ACCODED");
		builder.addConnection("decoder_parser_parseheaders", "ACCODED", "decoder_parser_splitter_BTYPE", "ACCODED");
		builder.addConnection("decoder_parser_parseheaders", "MOTION", "decoder_parser_mvseq", "MOTION");
		builder.addConnection("decoder_parser_parseheaders", "MOTION", "decoder_parser_splitter_MV", "MOTION");
		builder.addConnection("decoder_parser_parseheaders", "MOTION", "decoder_parser_mvrecon", "MOTION");
		builder.addConnection("decoder_parser_parseheaders", "MOTION", "decoder_parser_splitter_BTYPE", "MOTION");
		builder.addConnection("decoder_parser_parseheaders", "ACPRED", "decoder_parser_splitter_BTYPE", "ACPRED");
		builder.addConnection("decoder_parser_parseheaders", "FOUR_MV", "decoder_parser_mvseq", "FOUR_MV");
		builder.addConnection("decoder_parser_parseheaders", "FOUR_MV", "decoder_parser_mvrecon", "FOUR_MV");
		builder.addConnection("decoder_parser_parseheaders", "BTYPE", "decoder_parser_splitter_BTYPE", "BTYPE");
		builder.addConnection("decoder_parser_parseheaders", "BTYPE", "decoder_parser_mvseq", "BTYPE");
		builder.addConnection("decoder_parser_parseheaders", "BTYPE", "decoder_parser_splitter_420_B", "BTYPE");
		builder.addConnection("decoder_parser_parseheaders", "BTYPE", "decoder_parser_mvrecon", "BTYPE");
		builder.addConnection("decoder_parser_parseheaders", "QP", "decoder_parser_splitter_BTYPE", "QP");
		builder.addConnection("decoder_serialize", "BITS", "decoder_parser_parseheaders", "BITS");
		builder.addConnection("decoder_texture_Y_DCsplit", "AC", "decoder_texture_Y_IS", "QFS_AC");
		builder.addConnection("decoder_texture_Y_IS", "PQF_AC", "decoder_texture_Y_IAP", "PQF_AC");
		builder.addConnection("decoder_texture_Y_IAP", "QF_AC", "decoder_texture_Y_IQ", "AC");
		builder.addConnection("decoder_texture_Y_IQ", "OUT", "decoder_texture_Y_idct2d", "IN");
		builder.addConnection("decoder_texture_Y_DCRecontruction_addressing", "A", "decoder_texture_Y_DCRecontruction_invpred", "A");
		builder.addConnection("decoder_texture_Y_DCRecontruction_addressing", "B", "decoder_texture_Y_DCRecontruction_invpred", "B");
		builder.addConnection("decoder_texture_Y_DCRecontruction_addressing", "C", "decoder_texture_Y_DCRecontruction_invpred", "C");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "QUANT", "decoder_texture_Y_IQ", "QP");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "QF_DC", "decoder_texture_Y_IQ", "DC");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "PTR", "decoder_texture_Y_IAP", "PTR");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_Y_IAP", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_Y_IS", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "SIGNED", "decoder_texture_Y_idct2d", "SIGNED");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "QUANT", "decoder_texture_Y_IAP", "QP");
		builder.addConnection("decoder_texture_Y_DCRecontruction_invpred", "PREV_QUANT", "decoder_texture_Y_IAP", "PREV_QP");
		builder.addConnection("decoder_texture_Y_DCsplit", "DC", "decoder_texture_Y_DCRecontruction_invpred", "QFS_DC");
		builder.addConnection("decoder_parser_splitter_420_B", "B_Y", "decoder_texture_Y_DCsplit", "IN");
		builder.addConnection("decoder_parser_splitter_BTYPE", "QP_Y", "decoder_texture_Y_DCRecontruction_invpred", "QP");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_Y_DCRecontruction_addressing", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_Y_DCRecontruction_invpred", "WIDTH");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_Y", "decoder_texture_Y_DCRecontruction_invpred", "AC_CODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACPRED_Y", "decoder_texture_Y_DCRecontruction_invpred", "AC_PRED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_Y", "decoder_texture_Y_DCRecontruction_addressing", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_Y", "decoder_texture_Y_DCRecontruction_invpred", "BTYPE");
		builder.addConnection("decoder_texture_U_DCsplit", "AC", "decoder_texture_U_IS", "QFS_AC");
		builder.addConnection("decoder_texture_U_IS", "PQF_AC", "decoder_texture_U_IAP", "PQF_AC");
		builder.addConnection("decoder_texture_U_IAP", "QF_AC", "decoder_texture_U_IQ", "AC");
		builder.addConnection("decoder_texture_U_IQ", "OUT", "decoder_texture_U_idct2d", "IN");
		builder.addConnection("decoder_texture_U_DCRecontruction_addressing", "A", "decoder_texture_U_DCRecontruction_invpred", "A");
		builder.addConnection("decoder_texture_U_DCRecontruction_addressing", "B", "decoder_texture_U_DCRecontruction_invpred", "B");
		builder.addConnection("decoder_texture_U_DCRecontruction_addressing", "C", "decoder_texture_U_DCRecontruction_invpred", "C");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "SIGNED", "decoder_texture_U_idct2d", "SIGNED");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "QUANT", "decoder_texture_U_IQ", "QP");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "QF_DC", "decoder_texture_U_IQ", "DC");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "PTR", "decoder_texture_U_IAP", "PTR");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_U_IAP", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_U_IS", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "QUANT", "decoder_texture_U_IAP", "QP");
		builder.addConnection("decoder_texture_U_DCRecontruction_invpred", "PREV_QUANT", "decoder_texture_U_IAP", "PREV_QP");
		builder.addConnection("decoder_texture_U_DCsplit", "DC", "decoder_texture_U_DCRecontruction_invpred", "QFS_DC");
		builder.addConnection("decoder_parser_splitter_420_B", "B_U", "decoder_texture_U_DCsplit", "IN");
		builder.addConnection("decoder_parser_splitter_BTYPE", "QP_U", "decoder_texture_U_DCRecontruction_invpred", "QP");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_U_DCRecontruction_addressing", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_U_DCRecontruction_invpred", "WIDTH");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_U", "decoder_texture_U_DCRecontruction_invpred", "AC_CODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACPRED_U", "decoder_texture_U_DCRecontruction_invpred", "AC_PRED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_U", "decoder_texture_U_DCRecontruction_addressing", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_U", "decoder_texture_U_DCRecontruction_invpred", "BTYPE");
		builder.addConnection("decoder_texture_V_DCsplit", "AC", "decoder_texture_V_IS", "QFS_AC");
		builder.addConnection("decoder_texture_V_IS", "PQF_AC", "decoder_texture_V_IAP", "PQF_AC");
		builder.addConnection("decoder_texture_V_IAP", "QF_AC", "decoder_texture_V_IQ", "AC");
		builder.addConnection("decoder_texture_V_IQ", "OUT", "decoder_texture_V_idct2d", "IN");
		builder.addConnection("decoder_texture_V_DCRecontruction_addressing", "A", "decoder_texture_V_DCRecontruction_invpred", "A");
		builder.addConnection("decoder_texture_V_DCRecontruction_addressing", "B", "decoder_texture_V_DCRecontruction_invpred", "B");
		builder.addConnection("decoder_texture_V_DCRecontruction_addressing", "C", "decoder_texture_V_DCRecontruction_invpred", "C");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "SIGNED", "decoder_texture_V_idct2d", "SIGNED");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "QUANT", "decoder_texture_V_IQ", "QP");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "QF_DC", "decoder_texture_V_IQ", "DC");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "PTR", "decoder_texture_V_IAP", "PTR");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_V_IAP", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "AC_PRED_DIR", "decoder_texture_V_IS", "AC_PRED_DIR");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "QUANT", "decoder_texture_V_IAP", "QP");
		builder.addConnection("decoder_texture_V_DCRecontruction_invpred", "PREV_QUANT", "decoder_texture_V_IAP", "PREV_QP");
		builder.addConnection("decoder_texture_V_DCsplit", "DC", "decoder_texture_V_DCRecontruction_invpred", "QFS_DC");
		builder.addConnection("decoder_parser_splitter_420_B", "B_V", "decoder_texture_V_DCsplit", "IN");
		builder.addConnection("decoder_parser_splitter_BTYPE", "QP_V", "decoder_texture_V_DCRecontruction_invpred", "QP");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_V_DCRecontruction_addressing", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_texture_V_DCRecontruction_invpred", "WIDTH");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_V", "decoder_texture_V_DCRecontruction_invpred", "AC_CODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACPRED_V", "decoder_texture_V_DCRecontruction_invpred", "AC_PRED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_V", "decoder_texture_V_DCRecontruction_addressing", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_V", "decoder_texture_V_DCRecontruction_invpred", "BTYPE");
		builder.addConnection("decoder_motion_Y_interpolation", "MOT", "decoder_motion_Y_add", "MOT");
		builder.addConnection("decoder_motion_Y_FrameBuff", "RD", "decoder_motion_Y_interpolation", "RD");
		builder.addConnection("decoder_motion_Y_FrameBuff", "halfpel", "decoder_motion_Y_interpolation", "halfpel");
		builder.addConnection("decoder_motion_Y_add", "VID", "decoder_motion_Y_FrameBuff", "WD");
		builder.addConnection("decoder_motion_Y_add", "VID", "decoder_Merger420", "Y");
		builder.addConnection("decoder_parser_splitter_MV", "MV_Y", "decoder_motion_Y_FrameBuff", "MV");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_motion_Y_FrameBuff", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "HEIGHT", "decoder_motion_Y_FrameBuff", "HEIGHT");
		builder.addConnection("decoder_parser_parseheaders", "ROUND", "decoder_motion_Y_FrameBuff", "ROUND");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_Y", "decoder_motion_Y_add", "ACCODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "MOTION_Y", "decoder_motion_Y_FrameBuff", "MOT");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_Y", "decoder_motion_Y_add", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_Y", "decoder_motion_Y_FrameBuff", "BTYPE");
		builder.addConnection("decoder_texture_Y_idct2d", "OUT", "decoder_motion_Y_add", "TEX");
		builder.addConnection("decoder_motion_U_interpolation", "MOT", "decoder_motion_U_add", "MOT");
		builder.addConnection("decoder_motion_U_FrameBuff", "RD", "decoder_motion_U_interpolation", "RD");
		builder.addConnection("decoder_motion_U_FrameBuff", "halfpel", "decoder_motion_U_interpolation", "halfpel");
		builder.addConnection("decoder_motion_U_add", "VID", "decoder_motion_U_FrameBuff", "WD");
		builder.addConnection("decoder_motion_U_add", "VID", "decoder_Merger420", "U");
		builder.addConnection("decoder_parser_splitter_MV", "MV_U", "decoder_motion_U_FrameBuff", "MV");
		builder.addConnection("decoder_parser_parseheaders", "HEIGHT", "decoder_motion_U_FrameBuff", "HEIGHT");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_motion_U_FrameBuff", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "ROUND", "decoder_motion_U_FrameBuff", "ROUND");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_U", "decoder_motion_U_add", "ACCODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "MOTION_U", "decoder_motion_U_FrameBuff", "MOT");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_U", "decoder_motion_U_add", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_U", "decoder_motion_U_FrameBuff", "BTYPE");
		builder.addConnection("decoder_texture_U_idct2d", "OUT", "decoder_motion_U_add", "TEX");
		builder.addConnection("decoder_motion_V_interpolation", "MOT", "decoder_motion_V_add", "MOT");
		builder.addConnection("decoder_motion_V_FrameBuff", "RD", "decoder_motion_V_interpolation", "RD");
		builder.addConnection("decoder_motion_V_FrameBuff", "halfpel", "decoder_motion_V_interpolation", "halfpel");
		builder.addConnection("decoder_motion_V_add", "VID", "decoder_motion_V_FrameBuff", "WD");
		builder.addConnection("decoder_motion_V_add", "VID", "decoder_Merger420", "V");
		builder.addConnection("decoder_parser_splitter_MV", "MV_V", "decoder_motion_V_FrameBuff", "MV");
		builder.addConnection("decoder_parser_parseheaders", "WIDTH", "decoder_motion_V_FrameBuff", "WIDTH");
		builder.addConnection("decoder_parser_parseheaders", "HEIGHT", "decoder_motion_V_FrameBuff", "HEIGHT");
		builder.addConnection("decoder_parser_parseheaders", "ROUND", "decoder_motion_V_FrameBuff", "ROUND");
		builder.addConnection("decoder_parser_splitter_BTYPE", "ACCODED_V", "decoder_motion_V_add", "ACCODED");
		builder.addConnection("decoder_parser_splitter_BTYPE", "MOTION_V", "decoder_motion_V_FrameBuff", "MOT");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_V", "decoder_motion_V_add", "BTYPE");
		builder.addConnection("decoder_parser_splitter_BTYPE", "BTYPE_V", "decoder_motion_V_FrameBuff", "BTYPE");
		builder.addConnection("decoder_texture_V_idct2d", "OUT", "decoder_motion_V_add", "TEX");
		builder.addConnection("decoder_Merger420", "YUV", null, "OUTPUT");
		builder.addConnection(null, "INPUT", "decoder_serialize", "BYTE");
	}

	public static void main(String[] args) throws FileNotFoundException {
		TestDecoder test = new TestDecoder("../orc-apps/RVC/src/");
		Network network = test.network();
		PrintWriter writer = new PrintWriter("decoder.c");
		Backend.generateCode(network, writer);
	}

}
