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
	FIR v1 illustrates the construction of a simple FIR filter in CAL.
	
	The Top testbed generates a noisy sine wave and instantiates the FIR filter
	to filter it. It plots the comparison between the input and the output signal.
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
	
	tagOrig = Tag(tag = "Original");
	tagFIR = Tag(tag = "FIR");
	
	fir = FIR(taps = taps);

structure
	clk.Out --> r.Trigger;
	clk.Out --> s.Trigger;	
	r.Out --> mul.In;
	mul.Out --> add.A;
	s.Out --> add.B;
	add.Out --> tagOrig.In;
	
	add.Out --> fir.In;
	fir.Out --> tagFIR.In;
	
	tagOrig.Out --> p.Data;	
	tagFIR.Out --> p.Data;
end		