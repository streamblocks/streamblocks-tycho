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

network FIR (taps, nUnits) In ==> Out :

var
	nTaps = #taps;
	
	function reverse (a) : [a[#a - i] : for i in 1 .. #a] end

	function max (a, b) : if a > b then a else b end end
 	
	function fold (a, n)
	var
		k = (#a + n - 1) / n 
	:
		[
			[taps[k * i + j] : for j in 0 .. k - 1, (k * i + j) < nTaps]
		: 
			for i in 0 .. n - 1
		]
	end
	
	tapSegments = fold(reverse(taps), nUnits);
	nSegs = #tapSegments;
		
entities
	c = [FIRcell(taps = segment) : for segment in reverse(tapSegments)];
	zeros = Constants(constants = [0]);
	
structure
	foreach i in 0 .. nSegs - 1 do
		In --> c[i].Sample;
		if i = 0 then
			zeros.Out --> c[i].AccLineIn;
		else
			c[i - 1].AccLineOut --> c[i].AccLineIn;
		end
	end
	In --> zeros.Trigger;
	c[nSegs - 1].AccLineOut --> Out;
end
	