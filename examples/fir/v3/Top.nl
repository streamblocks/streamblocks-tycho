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

/**
	FIR v3 illustrates the folding of an FIR filter onto a specified
	number of execution units.

	The Top testbed instatiates the folded semi-parallel implementation and a
	golden reference (FIR v1), and plots their output on the same noisy sine wave.
*/

network Top () ==> :

import entity net.sf.opendf.actors.Plotter;

var
	noise = .1;
	taps = [-.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609];

entities
	r = Random();
	mul = ConstantMultiply(c = noise);
	add = Add();
	s = Sine(d = .01);
	p = Plotter(autoredraw = 50);
	clk = Clock(dt = 1);
	
	tagFIRgolden = Tag(tag = "FIR golden");
	tagFIRfolded = Tag(tag = "FIR folded");

	firGolden = FIRgolden(taps = taps);	
	firFolded = FIR(taps = taps, nUnits = 3);
	
structure
	clk.Out --> r.Trigger;
	clk.Out --> s.Trigger;
	
	r.Out --> mul.In;
	mul.Out --> add.A;
	s.Out --> add.B;
	
	add.Out --> firGolden.In;
	firGolden.Out --> tagFIRgolden.In;
	tagFIRgolden.Out --> p.Data;
	
	add.Out --> firFolded.In;
	firFolded.Out --> tagFIRfolded.In;
	tagFIRfolded.Out --> p.Data;	
end		