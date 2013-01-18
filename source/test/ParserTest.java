import java.io.File;

import net.opendf.ir.cal.Actor;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.parser.lth.CalParser;
import net.opendf.parser.lth.NlParser;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *  A JUnit test case file that recursively traverse the file system, parsing all CAL and NL programs it encounter. The traversal starts in the directory given by the constant attribute initialPath.
 *  To run in eclipse: create a JUnit run configuration, set ${workspace_loc:CAL_parser} as the working directory (under the arguments(x) tab)
 **/
public class ParserTest extends TestCase{
	static final String initialPath = "examples";
	static final String usage = "Correct use: java ParserTest path";

	public void testValidCalProgram(){
		TravesingFileSystemTester();
//		fileListTester(allFiles);
//		fileListTester(topFiles);
	}

	// parse all files in the array "files"
 	public void fileListTester(String files[]){
		for(String fileName : files){
			System.out.println("parsing " + fileName);
			parseFile(new File(fileName));
		}
	}

 	// traverse the file system recursively and parse all files encountered.
 	public void TravesingFileSystemTester(){
		File f = new File(initialPath);
		addDir(f);
	}

 	public void addDir(File f){
		System.out.println("looking in directiry " + f.getPath());
		String[] child = f.list();
		if(child != null){
			for(int i=0;i<child.length;i++){
				File candidate = new File(f, child[i]);
				if(!candidate.isHidden()){
					if(candidate.isDirectory()){
						addDir(candidate);
					} else if(candidate.isFile()){
						parseFile(candidate);
					}
				}
			}
		}
	}

	public void parseFile(File file){
		String fileName = file.getName();
		if(fileName.endsWith(".cal")){
			System.out.println("  parsing cal-file " + file.getName());
			CalParser parser = new CalParser();
			Actor actor = parser.parse(file);
			if(!parser.parseProblems.isEmpty()){
				parser.printParseProblems(System.err);
				Assert.fail("compile error while loading " + fileName + " in directory " + file.getParent());
			}
		} else if(fileName.endsWith(".nl")){
			System.out.println("  parsing nl-file " + file.getName());
			NlParser parser = new NlParser();
			NetworkDefinition network = parser.parse(file);
			if(!parser.parseProblems.isEmpty()){
				parser.printParseProblems();
				Assert.fail("compile error while loading " + fileName + " in directory " + file.getParent());
			}
		}
	}
	static String[] topFiles = {
//		"data/fft/v0/Top.nl",  //Math.sin(...)
//		"data/fft/v1/Top.nl",
//		"data/fir/v2/Top.nl",
//		"data/fir/v3/Top.nl",
//		"data/fir/v4/Top.nl",
//		"data/Game Of Life/v1/Top_no_dispaly.nl",  //type error due to system function randint()
//		"data/Game Of Life/v1/Top.nl",  //dispaly.add()
//		"data/Mandelbrot/v1/TopMandelbrot.nl",
//		"data/Mandelbrot/v1/TopMandelbrotPartial.nl",
//		"data/Mandelbrot/v2/TopShow.nl",
//		"data/MiniDataflowProcessor/TopFibonacci.nl",
		"data/MPEG4_SP_Decoder/decoder.nl",
		"data/my_example/Top.nl",
		"data/SimpleExamples/Fibs.nl",
		"data/SimpleExamples/Fibs3.nl",
		"data/SimpleExamples/Fibs5.nl",
//		"data/SimpleExamples/FibsN/Test.nl",  //type error due to ActorParameter of unknown type
		"data/SimpleExamples/Nats.nl",
//		"data/SimpleExamples/Primes/Top.nl",
		"data/SimpleExamples/Sum.nl",
//		"data/SphereDetector/Top.nl",
//		"data/Video_Resizer/hobsonTestbed.nl",
//		"data/Video_Resizer/Resizer.nl",
//		"data/Video_Resizer/resizerTestbed.nl",
    };
	static String[] allFiles = {
		"data/fft/v0/Add.cal",
		"data/fft/v0/Clock.cal",
		"data/fft/v0/dft.cal",
		"data/fft/v0/idft.cal",
		"data/fft/v0/Sine.cal",
		"data/fft/v0/Tag.cal",
		"data/fft/v0/Top.nl",
		"data/fft/v1/Add.cal",
		"data/fft/v1/Butterfly.nl",
		"data/fft/v1/ButterflyFFT.nl",
		"data/fft/v1/Clock.cal",
		"data/fft/v1/ConstantMultiply.cal",
		"data/fft/v1/dft.cal",
		"data/fft/v1/Merge.cal",
		"data/fft/v1/Radix2Cell.cal",
		"data/fft/v1/Sine.cal",
		"data/fft/v1/Split.cal",
		"data/fft/v1/Tag.cal",
		"data/fft/v1/Top.nl",
		"data/fft/v1/TwiddleGenerator.cal",
		"data/fir/v1/Add.cal",
		"data/fir/v1/Clock.cal",
		"data/fir/v1/ConstantMultiply.cal",
		"data/fir/v1/fir.cal",
		"data/fir/v1/Random.cal",
		"data/fir/v1/Sine.cal",
		"data/fir/v1/Tag.cal",
		"data/fir/v1/Top.nl",
		"data/fir/v2/Add.cal",
		"data/fir/v2/Clock.cal",
		"data/fir/v2/ConstantMultiply.cal",
		"data/fir/v2/Constants.cal",
		"data/fir/v2/fir.nl",
		"data/fir/v2/FIRgolden.cal",
		"data/fir/v2/Multiply.cal",
		"data/fir/v2/Random.cal",
		"data/fir/v2/Sine.cal",
		"data/fir/v2/Tag.cal",
		"data/fir/v2/Top.nl",
		"data/fir/v2/z.cal",
		"data/fir/v3/Add.cal",
		"data/fir/v3/Clock.cal",
		"data/fir/v3/ConstantMultiply.cal",
		"data/fir/v3/Constants.cal",
		"data/fir/v3/fir.nl",
		"data/fir/v3/FIRcell.cal",
		"data/fir/v3/FIRgolden.cal",
		"data/fir/v3/Random.cal",
		"data/fir/v3/Sine.cal",
		"data/fir/v3/Tag.cal",
		"data/fir/v3/Top.nl",
		"data/fir/v4/Add.cal",
		"data/fir/v4/Clock.cal",
		"data/fir/v4/ConstantMultiply.cal",
		"data/fir/v4/Constants.cal",
		"data/fir/v4/fir.nl",
		"data/fir/v4/FIRcell.nl",
		"data/fir/v4/FIRgolden.cal",
		"data/fir/v4/MergeLast.cal",
		"data/fir/v4/Multiply.cal",
		"data/fir/v4/Random.cal",
		"data/fir/v4/Sine.cal",
		"data/fir/v4/SplitFirst.cal",
		"data/fir/v4/Tag.cal",
		"data/fir/v4/Top.nl",
		"data/fir/v4/Upsample.cal",
		"data/fir/v4/z.cal",
		"data/Game Of Life/v0/Cell.cal",
		"data/Game Of Life/v0/Edge.cal",
		"data/Game Of Life/v0/GoL.nl",
		"data/Game Of Life/v1/Cell.cal",
		"data/Game Of Life/v1/Display.cal",
		"data/Game Of Life/v1/Edge.cal",
		"data/Game Of Life/v1/GoL.nl",
		"data/Game Of Life/v1/Mapper.cal",
		"data/Game Of Life/v1/Top.nl",
		"data/Mandelbrot/v1/Clock.cal",
		"data/Mandelbrot/v1/Display.cal",
		"data/Mandelbrot/v1/MandelbrotCoordinateGenerator.cal",
		"data/Mandelbrot/v1/MandelbrotKernel.cal",
		"data/Mandelbrot/v1/SampleColorer.cal",
		"data/Mandelbrot/v1/TopMandelbrot.nl",
		"data/Mandelbrot/v1/TopMandelbrotPartial.nl",
		"data/Mandelbrot/v2/b2w.cal",
		"data/Mandelbrot/v2/Clock.cal",
		"data/Mandelbrot/v2/CoordinateGenerator.cal",
		"data/Mandelbrot/v2/Display.cal",
		"data/Mandelbrot/v2/MandelbrotCoordinateGenerator.cal",
		"data/Mandelbrot/v2/MandelbrotKernel.cal",
		"data/Mandelbrot/v2/ProgressWindow.cal",
		"data/Mandelbrot/v2/ReadFile.cal",
		"data/Mandelbrot/v2/SampleColorer.cal",
		"data/Mandelbrot/v2/TopComputeMandelbrot.nl",
		"data/Mandelbrot/v2/TopShow.nl",
		"data/Mandelbrot/v2/TopWritePNG.nl",
		"data/Mandelbrot/v2/w2b.cal",
		"data/Mandelbrot/v2/WriteFile.cal",
		"data/Mandelbrot/v2/WriteImage.cal",
		"data/MiniDataflowProcessor/alu.cal",
		"data/MiniDataflowProcessor/Cache0.cal",
		"data/MiniDataflowProcessor/cpu.nl",
		"data/MiniDataflowProcessor/InstructionDecoder.cal",
		"data/MiniDataflowProcessor/mdp.nl",
		"data/MiniDataflowProcessor/Memory.cal",
		"data/MiniDataflowProcessor/MemoryAddressGenerator.cal",
		"data/MiniDataflowProcessor/pc.cal",
		"data/MiniDataflowProcessor/Print.cal",
		"data/MiniDataflowProcessor/RegisterFile.cal",
		"data/MiniDataflowProcessor/rom.cal",
		"data/MiniDataflowProcessor/SerialInput.cal",
		"data/MiniDataflowProcessor/SerialOutput.cal",
		"data/MiniDataflowProcessor/TopFibonacci.nl",
		"data/MPEG4_SP_Decoder/acdc.nl",
		"data/MPEG4_SP_Decoder/ACPred.cal",
		"data/MPEG4_SP_Decoder/Add.cal",
		"data/MPEG4_SP_Decoder/BlockExpand.cal",
		"data/MPEG4_SP_Decoder/byte2bit.cal",
		"data/MPEG4_SP_Decoder/Clip.cal",
		"data/MPEG4_SP_Decoder/Combine.cal",
		"data/MPEG4_SP_Decoder/Compare.cal",
		"data/MPEG4_SP_Decoder/DCPred.cal",
		"data/MPEG4_SP_Decoder/DCSplit.cal",
		"data/MPEG4_SP_Decoder/DDRModel.cal",
		"data/MPEG4_SP_Decoder/decoder.nl",
		"data/MPEG4_SP_Decoder/Dequant.cal",
		"data/MPEG4_SP_Decoder/DispYUV.cal",
		"data/MPEG4_SP_Decoder/Downsample.cal",
		"data/MPEG4_SP_Decoder/FairMerge.cal",
		"data/MPEG4_SP_Decoder/Final.cal",
		"data/MPEG4_SP_Decoder/fread.cal",
		"data/MPEG4_SP_Decoder/idct1d.nl",
		"data/MPEG4_SP_Decoder/idct2d.nl",
		"data/MPEG4_SP_Decoder/Interpolate.cal",
		"data/MPEG4_SP_Decoder/MBPacker.cal",
		"data/MPEG4_SP_Decoder/MemoryManager.cal",
		"data/MPEG4_SP_Decoder/motion3.nl",
		"data/MPEG4_SP_Decoder/MVReconstruct.cal",
		"data/MPEG4_SP_Decoder/MVSequence.cal",
		"data/MPEG4_SP_Decoder/ParseHeaders.cal",
		"data/MPEG4_SP_Decoder/parser.nl",
		"data/MPEG4_SP_Decoder/Retranspose.cal",
		"data/MPEG4_SP_Decoder/RowSort.cal",
		"data/MPEG4_SP_Decoder/Scale.cal",
		"data/MPEG4_SP_Decoder/SearchWindow.cal",
		"data/MPEG4_SP_Decoder/SendDC.cal",
		"data/MPEG4_SP_Decoder/Separate.cal",
		"data/MPEG4_SP_Decoder/Sequence.cal",
		"data/MPEG4_SP_Decoder/Shuffle.cal",
		"data/MPEG4_SP_Decoder/ShuffleFly.cal",
		"data/MPEG4_SP_Decoder/testbed.nl",
		"data/MPEG4_SP_Decoder/Transpose.cal",
		"data/MPEG4_SP_Decoder/Unpack.cal",
		"data/MPEG4_SP_Decoder/Zigzag.cal",
		"data/MPEG4_SP_Decoder/ZigzagAddr.cal",
		"data/my_example/Add.cal",
		"data/my_example/Middle.nl",
		"data/my_example/Sub.cal",
		"data/my_example/Top.nl",
		"data/SimpleExamples/Add.cal",
		"data/SimpleExamples/Fibs.nl",
		"data/SimpleExamples/Fibs3.nl",
		"data/SimpleExamples/Fibs5.nl",
		"data/SimpleExamples/FibsN/Add.cal",
		"data/SimpleExamples/FibsN/Fib.nl",
		"data/SimpleExamples/FibsN/Fib3.nl",
		"data/SimpleExamples/FibsN/FibN.nl",
		"data/SimpleExamples/FibsN/Print.cal",
		"data/SimpleExamples/FibsN/Test.nl",
		"data/SimpleExamples/FibsN/Z.cal",
		"data/SimpleExamples/FibsN.nl",
		"data/SimpleExamples/InitialTokens.cal",
		"data/SimpleExamples/Nats.nl",
		"data/SimpleExamples/Primes/Generate.cal",
		"data/SimpleExamples/Primes/InitialTokens.cal",
		"data/SimpleExamples/Primes/Primes.nl",
		"data/SimpleExamples/Primes/Print.cal",
		"data/SimpleExamples/Primes/Sieve.cal",
		"data/SimpleExamples/Primes/Top.nl",
		"data/SimpleExamples/Sum.nl",
		"data/SphereDetector/InitialTokens.cal",
		"data/SphereDetector/kBest/Expand.cal",
		"data/SphereDetector/kBest/SDkbest.nl",
		"data/SphereDetector/kBest/Select.cal",
		"data/SphereDetector/kBest/StripS.cal",
		"data/SphereDetector/mlsd.cal",
		"data/SphereDetector/Print.cal",
		"data/SphereDetector/Top.nl",
		"data/Video_Resizer/bigFifo.nl",
		"data/Video_Resizer/bitPad.cal",
		"data/Video_Resizer/chop.cal",
		"data/Video_Resizer/chromaUpsample.cal",
		"data/Video_Resizer/clip.cal",
		"data/Video_Resizer/cmdStuffer.cal",
		"data/Video_Resizer/coeff.cal",
		"data/Video_Resizer/cropper.cal",
		"data/Video_Resizer/doubler.cal",
		"data/Video_Resizer/dram.cal",
		"data/Video_Resizer/ExtractSOF.cal",
		"data/Video_Resizer/fifoArbiter.cal",
		"data/Video_Resizer/fifoData.cal",
		"data/Video_Resizer/Filter.nl",
		"data/Video_Resizer/Filter7.cal",
		"data/Video_Resizer/Hobson.nl",
		"data/Video_Resizer/hobsonTestbed.nl",
		"data/Video_Resizer/hobsonTestSink.cal",
		"data/Video_Resizer/hobsonTestSource.cal",
		"data/Video_Resizer/horizPhaseCounter.cal",
		"data/Video_Resizer/HResizer.nl",
		"data/Video_Resizer/linebuf.cal",
		"data/Video_Resizer/linebufControl.cal",
		"data/Video_Resizer/pad.cal",
		"data/Video_Resizer/register.cal",
		"data/Video_Resizer/registerControl.cal",
		"data/Video_Resizer/repack.cal",
		"data/Video_Resizer/Resizer.nl",
		"data/Video_Resizer/resizerTestbed.nl",
		"data/Video_Resizer/resizerTestSink.cal",
		"data/Video_Resizer/resizerTestSource.cal",
		"data/Video_Resizer/unpack.cal",
		"data/Video_Resizer/unpack444.cal",
		"data/Video_Resizer/verticalPhaseCounter.cal",
		"data/Video_Resizer/VResizer.nl"
	};
}