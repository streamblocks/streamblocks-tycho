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
	FFT v1 illustrates the construction of a simple DFT in CAL.
	
*/

network Top () ==> :

import entity net.sf.opendf.actors.Plotter;

var
	F1 = 20.0;
	F2 = 30.0;
	F3 = 70.0;

	N = 256;
	SQRTN = 16;
	
entities
	add1 = Add();
	add2 = Add();
	s1 = Sine(d = (2.0 * 3.14) / F1);
	s2 = Sine(d = (2.0 * 3.14) / F2);
	s3 = Sine(d = (2.0 * 3.14) / F3);
	p = Plotter(autoredraw = 50, time = false);
	clk = Clock(dt = 1);
	
	tagOrig = Tag(tag = "Original");
	tagDFT = Tag(tag = "DFT");
	tagIDFT = Tag(tag = "IDFT");

	dft = DFT(N = N, scale = 1.0/SQRTN);
	idft = IDFT(N = N, scale = 1.0/SQRTN);

structure
	clk.Out --> s1.Trigger;
	clk.Out --> s2.Trigger;	
	clk.Out --> s3.Trigger;	
	s1.Out --> add1.A;
	s2.Out --> add1.B;
	add1.Out --> add2.A;
	s3.Out --> add2.B;
	add2.Out --> tagOrig.In;
	
	add2.Out --> dft.In;
	dft.Out --> tagDFT.In;
	
	dft.Out --> idft.In;
	idft.Out --> tagIDFT.In;
	
	tagOrig.Out --> p.Data;	
	tagDFT.Out --> p.Data;
	tagIDFT.Out --> p.Data;
end


		