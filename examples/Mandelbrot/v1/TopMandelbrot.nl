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

network TopMandelbrot (W = 300, H = 200, maxIter = 100, satLevels = 20) ==> :

	
entities

	clk = Clock();
	display = Display(title = "Mandelbrot", width = W, height = H, autoUpdate = 100);
	mcg = MandelbrotCoordinateGenerator(startX = -2.0, 
										startY = -1.0,
										dx = 3.0 / W,
										w = W,
										dy = 2.0 / H,
										h = H);
	kernel = MandelbrotKernel(maxIter = maxIter);
	colorer = SampleColorer(maxIter = maxIter, satLevels = satLevels);
	
structure

	clk.Out --> mcg.Trigger;
	
	mcg.X0 --> kernel.X0;
	mcg.Y0 --> kernel.Y0;
	mcg.X --> display.X;
	mcg.Y --> display.Y;
	mcg.DONE --> clk.Stop;
	
	kernel.N --> colorer.N;
	
	colorer.R --> display.R;
	colorer.G --> display.G;
	colorer.B --> display.B;
	
end
	


