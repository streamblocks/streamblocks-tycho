import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.parser.lth.CalParser;
import se.lth.cs.tycho.parser.lth.NlParser;

import org.junit.Assert;

/**
 *  A JUnit test case file that recursively traverse the file system, parsing all CAL and NL programs it encounter. The traversal starts in the directory given by the constant attribute initialPath.
 *  To run in eclipse: create a JUnit run configuration, set ${workspace_loc:CAL_parser} as the working directory (under the arguments(x) tab)
 **/
public class ParserTest {
	static final String initialPath = "../examples";
	static final String usage = "Correct use: java ParserTest path";

	@Ignore("tests the old parser")
	@Test
	public void testValidCalProgram(){
		TravesingFileSystemTester();
		fileListTester(allFiles);
		fileListTester(topFiles);
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
			CalActor calActor = (CalActor) parser.parse(file, null, null).getEntity();
			if(parser.getErrorModule().hasProblem()){
				parser.getErrorModule().printMessages();
				Assert.fail("compile error while loading " + fileName + " in directory " + file.getParent());
			}
		} else if(fileName.endsWith(".nl")){
			System.out.println("  parsing nl-file " + file.getName());
			NlParser parser = new NlParser();
			NlNetwork network = (NlNetwork) parser.parse(file, null, null).getEntity();
			if(parser.getErrorModule().hasProblem()){
				parser.getErrorModule().printMessages();
				Assert.fail("compile error while loading " + fileName + " in directory " + file.getParent());
			}
		}
	}
	static String[] topFiles = {
//		"fft/v0/Top.nl",  //Math.sin(...)
//		"fft/v1/Top.nl",
//		"fir/v2/Top.nl",
//		"fir/v3/Top.nl",
//		"fir/v4/Top.nl",
//		"Game Of Life/v1/Top_no_dispaly.nl",  //type error due to system function randint()
//		"Game Of Life/v1/Top.nl",  //dispaly.add()
//		"Mandelbrot/v1/TopMandelbrot.nl",
//		"Mandelbrot/v1/TopMandelbrotPartial.nl",
//		"Mandelbrot/v2/TopShow.nl",
//		"MiniDataflowProcessor/TopFibonacci.nl",
		"MPEG4_SP_Decoder/decoder.nl",
		"my_example/Top.nl",
		"SimpleExamples/Fibs.nl",
		"SimpleExamples/Fibs3.nl",
		"SimpleExamples/Fibs5.nl",
//		"SimpleExamples/FibsN/Test.nl",  //type error due to ActorParameter of unknown type
		"SimpleExamples/Nats.nl",
//		"SimpleExamples/Primes/Top.nl",
		"SimpleExamples/Sum.nl",
//		"SphereDetector/Top.nl",
//		"Video_Resizer/hobsonTestbed.nl",
//		"Video_Resizer/Resizer.nl",
//		"Video_Resizer/resizerTestbed.nl",
    };
	static String[] allFiles = {
		"fft/v0/Add.cal",
		"fft/v0/Clock.cal",
		"fft/v0/dft.cal",
		"fft/v0/idft.cal",
		"fft/v0/Sine.cal",
		"fft/v0/Tag.cal",
		"fft/v0/Top.nl",
		"fft/v1/Add.cal",
		"fft/v1/Butterfly.nl",
		"fft/v1/ButterflyFFT.nl",
		"fft/v1/Clock.cal",
		"fft/v1/ConstantMultiply.cal",
		"fft/v1/dft.cal",
		"fft/v1/Merge.cal",
		"fft/v1/Radix2Cell.cal",
		"fft/v1/Sine.cal",
		"fft/v1/Split.cal",
		"fft/v1/Tag.cal",
		"fft/v1/Top.nl",
		"fft/v1/TwiddleGenerator.cal",
		"fir/v1/Add.cal",
		"fir/v1/Clock.cal",
		"fir/v1/ConstantMultiply.cal",
		"fir/v1/fir.cal",
		"fir/v1/Random.cal",
		"fir/v1/Sine.cal",
		"fir/v1/Tag.cal",
		"fir/v1/Top.nl",
		"fir/v2/Add.cal",
		"fir/v2/Clock.cal",
		"fir/v2/ConstantMultiply.cal",
		"fir/v2/Constants.cal",
		"fir/v2/fir.nl",
		"fir/v2/FIRgolden.cal",
		"fir/v2/Multiply.cal",
		"fir/v2/Random.cal",
		"fir/v2/Sine.cal",
		"fir/v2/Tag.cal",
		"fir/v2/Top.nl",
		"fir/v2/z.cal",
		"fir/v3/Add.cal",
		"fir/v3/Clock.cal",
		"fir/v3/ConstantMultiply.cal",
		"fir/v3/Constants.cal",
		"fir/v3/fir.nl",
		"fir/v3/FIRcell.cal",
		"fir/v3/FIRgolden.cal",
		"fir/v3/Random.cal",
		"fir/v3/Sine.cal",
		"fir/v3/Tag.cal",
		"fir/v3/Top.nl",
		"fir/v4/Add.cal",
		"fir/v4/Clock.cal",
		"fir/v4/ConstantMultiply.cal",
		"fir/v4/Constants.cal",
		"fir/v4/fir.nl",
		"fir/v4/FIRcell.nl",
		"fir/v4/FIRgolden.cal",
		"fir/v4/MergeLast.cal",
		"fir/v4/Multiply.cal",
		"fir/v4/Random.cal",
		"fir/v4/Sine.cal",
		"fir/v4/SplitFirst.cal",
		"fir/v4/Tag.cal",
		"fir/v4/Top.nl",
		"fir/v4/Upsample.cal",
		"fir/v4/z.cal",
		"Game Of Life/v0/Cell.cal",
		"Game Of Life/v0/Edge.cal",
		"Game Of Life/v0/GoL.nl",
		"Game Of Life/v1/Cell.cal",
		"Game Of Life/v1/Display.cal",
		"Game Of Life/v1/Edge.cal",
		"Game Of Life/v1/GoL.nl",
		"Game Of Life/v1/Mapper.cal",
		"Game Of Life/v1/Top.nl",
		"Mandelbrot/v1/Clock.cal",
		"Mandelbrot/v1/Display.cal",
		"Mandelbrot/v1/MandelbrotCoordinateGenerator.cal",
		"Mandelbrot/v1/MandelbrotKernel.cal",
		"Mandelbrot/v1/SampleColorer.cal",
		"Mandelbrot/v1/TopMandelbrot.nl",
		"Mandelbrot/v1/TopMandelbrotPartial.nl",
		"Mandelbrot/v2/b2w.cal",
		"Mandelbrot/v2/Clock.cal",
		"Mandelbrot/v2/CoordinateGenerator.cal",
		"Mandelbrot/v2/Display.cal",
		"Mandelbrot/v2/MandelbrotCoordinateGenerator.cal",
		"Mandelbrot/v2/MandelbrotKernel.cal",
		"Mandelbrot/v2/ProgressWindow.cal",
		"Mandelbrot/v2/ReadFile.cal",
		"Mandelbrot/v2/SampleColorer.cal",
		"Mandelbrot/v2/TopComputeMandelbrot.nl",
		"Mandelbrot/v2/TopShow.nl",
		"Mandelbrot/v2/TopWritePNG.nl",
		"Mandelbrot/v2/w2b.cal",
		"Mandelbrot/v2/WriteFile.cal",
		"Mandelbrot/v2/WriteImage.cal",
		"MiniDataflowProcessor/alu.cal",
		"MiniDataflowProcessor/Cache0.cal",
		"MiniDataflowProcessor/cpu.nl",
		"MiniDataflowProcessor/InstructionDecoder.cal",
		"MiniDataflowProcessor/mdp.nl",
		"MiniDataflowProcessor/Memory.cal",
		"MiniDataflowProcessor/MemoryAddressGenerator.cal",
		"MiniDataflowProcessor/pc.cal",
		"MiniDataflowProcessor/Print.cal",
		"MiniDataflowProcessor/RegisterFile.cal",
		"MiniDataflowProcessor/rom.cal",
		"MiniDataflowProcessor/SerialInput.cal",
		"MiniDataflowProcessor/SerialOutput.cal",
		"MiniDataflowProcessor/TopFibonacci.nl",
		"MPEG4_SP_Decoder/acdc.nl",
		"MPEG4_SP_Decoder/ACPred.cal",
		"MPEG4_SP_Decoder/Add.cal",
		"MPEG4_SP_Decoder/BlockExpand.cal",
		"MPEG4_SP_Decoder/byte2bit.cal",
		"MPEG4_SP_Decoder/Clip.cal",
		"MPEG4_SP_Decoder/Combine.cal",
		"MPEG4_SP_Decoder/Compare.cal",
		"MPEG4_SP_Decoder/DCPred.cal",
		"MPEG4_SP_Decoder/DCSplit.cal",
		"MPEG4_SP_Decoder/DDRModel.cal",
		"MPEG4_SP_Decoder/decoder.nl",
		"MPEG4_SP_Decoder/Dequant.cal",
		"MPEG4_SP_Decoder/DispYUV.cal",
		"MPEG4_SP_Decoder/Downsample.cal",
		"MPEG4_SP_Decoder/FairMerge.cal",
		"MPEG4_SP_Decoder/Final.cal",
		"MPEG4_SP_Decoder/fread.cal",
		"MPEG4_SP_Decoder/idct1d.nl",
		"MPEG4_SP_Decoder/idct2d.nl",
		"MPEG4_SP_Decoder/Interpolate.cal",
		"MPEG4_SP_Decoder/MBPacker.cal",
		"MPEG4_SP_Decoder/MemoryManager.cal",
		"MPEG4_SP_Decoder/motion3.nl",
		"MPEG4_SP_Decoder/MVReconstruct.cal",
		"MPEG4_SP_Decoder/MVSequence.cal",
		"MPEG4_SP_Decoder/ParseHeaders.cal",
		"MPEG4_SP_Decoder/parser.nl",
		"MPEG4_SP_Decoder/Retranspose.cal",
		"MPEG4_SP_Decoder/RowSort.cal",
		"MPEG4_SP_Decoder/Scale.cal",
		"MPEG4_SP_Decoder/SearchWindow.cal",
		"MPEG4_SP_Decoder/SendDC.cal",
		"MPEG4_SP_Decoder/Separate.cal",
		"MPEG4_SP_Decoder/Sequence.cal",
		"MPEG4_SP_Decoder/Shuffle.cal",
		"MPEG4_SP_Decoder/ShuffleFly.cal",
		"MPEG4_SP_Decoder/testbed.nl",
		"MPEG4_SP_Decoder/Transpose.cal",
		"MPEG4_SP_Decoder/Unpack.cal",
		"MPEG4_SP_Decoder/Zigzag.cal",
		"MPEG4_SP_Decoder/ZigzagAddr.cal",
		"my_example/Add.cal",
		"my_example/Middle.nl",
		"my_example/Sub.cal",
		"my_example/Top.nl",
		"SimpleExamples/Add.cal",
		"SimpleExamples/Fibs.nl",
		"SimpleExamples/Fibs3.nl",
		"SimpleExamples/Fibs5.nl",
		"SimpleExamples/FibsN/Add.cal",
		"SimpleExamples/FibsN/Fib.nl",
		"SimpleExamples/FibsN/Fib3.nl",
		"SimpleExamples/FibsN/FibN.nl",
		"SimpleExamples/FibsN/Print.cal",
		"SimpleExamples/FibsN/Test.nl",
		"SimpleExamples/FibsN/Z.cal",
		"SimpleExamples/FibsN.nl",
		"SimpleExamples/InitialTokens.cal",
		"SimpleExamples/Nats.nl",
		"SimpleExamples/Primes/Generate.cal",
		"SimpleExamples/Primes/InitialTokens.cal",
		"SimpleExamples/Primes/Primes.nl",
		"SimpleExamples/Primes/Print.cal",
		"SimpleExamples/Primes/Sieve.cal",
		"SimpleExamples/Primes/Top.nl",
		"SimpleExamples/Sum.nl",
		"SphereDetector/InitialTokens.cal",
		"SphereDetector/kBest/Expand.cal",
		"SphereDetector/kBest/SDkbest.nl",
		"SphereDetector/kBest/Select.cal",
		"SphereDetector/kBest/StripS.cal",
		"SphereDetector/mlsd.cal",
		"SphereDetector/Print.cal",
		"SphereDetector/Top.nl",
		"Video_Resizer/bigFifo.nl",
		"Video_Resizer/bitPad.cal",
		"Video_Resizer/chop.cal",
		"Video_Resizer/chromaUpsample.cal",
		"Video_Resizer/clip.cal",
		"Video_Resizer/cmdStuffer.cal",
		"Video_Resizer/coeff.cal",
		"Video_Resizer/cropper.cal",
		"Video_Resizer/doubler.cal",
		"Video_Resizer/dram.cal",
		"Video_Resizer/ExtractSOF.cal",
		"Video_Resizer/fifoArbiter.cal",
		"Video_Resizer/fifoData.cal",
		"Video_Resizer/Filter.nl",
		"Video_Resizer/Filter7.cal",
		"Video_Resizer/Hobson.nl",
		"Video_Resizer/hobsonTestbed.nl",
		"Video_Resizer/hobsonTestSink.cal",
		"Video_Resizer/hobsonTestSource.cal",
		"Video_Resizer/horizPhaseCounter.cal",
		"Video_Resizer/HResizer.nl",
		"Video_Resizer/linebuf.cal",
		"Video_Resizer/linebufControl.cal",
		"Video_Resizer/pad.cal",
		"Video_Resizer/register.cal",
		"Video_Resizer/registerControl.cal",
		"Video_Resizer/repack.cal",
		"Video_Resizer/Resizer.nl",
		"Video_Resizer/resizerTestbed.nl",
		"Video_Resizer/resizerTestSink.cal",
		"Video_Resizer/resizerTestSource.cal",
		"Video_Resizer/unpack.cal",
		"Video_Resizer/unpack444.cal",
		"Video_Resizer/verticalPhaseCounter.cal",
		"Video_Resizer/VResizer.nl"
	};
}