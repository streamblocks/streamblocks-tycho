/* 
BEGINCOPYRIGHT X
	
	Copyright (c) 2007, Xilinx Inc.
	All rights reserved.
	
	Redistribution and use in source and binary forms, 
	with or without modification, are permitted provided 
	that the following conditions are met:
	- Redistributions of source code must retain the above 
	  copyright notice, this list of conditions and the 
	  following disclaimer.
	- Redistributions in binary form must reproduce the 
	  above copyright notice, this list of conditions and 
	  the following disclaimer in the documentation and/or 
	  other materials provided with the distribution.
	- Neither the name of the copyright holder nor the names 
	  of its contributors may be used to endorse or promote 
	  products derived from this software without specific 
	  prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
	CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
	HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
	OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	
ENDCOPYRIGHT
*/

network TopComputeMandelbrot (W, H, maxIter, CenterX, CenterY, DiameterX, DiameterY, fileName) ==> :

	// try these parameter values:
	//
	// 		W=1200, H=800, maxIter=1000
	// 		CenterX=-0.7435669, DiameterX=0.0022878
	// 		CenterY=0.1314023, DiameterY=(0.0022878*0.66) 
	// 		satLevels=50
	
entities

	clk = Clock();
	mcg = MandelbrotCoordinateGenerator(startX = CenterX - DiameterX / 2, 
										startY = CenterY - DiameterY / 2,
										dx = DiameterX / W,
										w = W,
										dy = DiameterY / H,
										h = H);
	kernel = MandelbrotKernel(maxIter = maxIter);

	w2b = W2B(bigEndian = true);
	writer = WriteFile(fname = fileName);
	progress = ProgressWindow(title = "Computing Mandelbrot set into file: " + fileName, 
	                          N = W * H);
	
structure

	clk.Out --> mcg.Trigger;
	
	mcg.X0 --> kernel.X0;
	mcg.Y0 --> kernel.Y0;
	mcg.DONE --> clk.Stop;
	mcg.DONE --> writer.Done;
	
	kernel.N --> w2b.In;
	kernel.N --> progress.Tick;
	
	w2b.Out --> writer.D;
	
end
	


