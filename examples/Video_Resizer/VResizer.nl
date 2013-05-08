/* 
BEGINCOPYRIGHT X
	
	Copyright (c) 2004-2005, Xilinx Inc.
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

// VResizer.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

network VResizer
(
  int PIXSZ,
  int PHASESZ,
  int COORDSZ,
  int ADDRSZ,
  int DATASZ,
  int COEFFSZ,
  int GUARDBITS
)
  IN, PARAM, WA, WD ==> OUT :

var
    
  int CMDSZ = 4;
  int PRELOAD = 1;
  int SHIFT = 2;
  int REPLICATE = 4;
  int EOL = 8;

  int NTAPS = 7;

  int FUDGE = 2;
  int regBufSize     = 2 * FUDGE;
  int lineBufSize    = 2048;
  int defaultBufSize = 4 * FUDGE;
  int regOutShort    = 8 * FUDGE;
  int regOutLong     = 8 * FUDGE;
  int ctrlBufSize    = 16 * FUDGE;
  
entities
  
  pc = verticalPhaseCounter( PHASESZ=PHASESZ, COORDSZ=COORDSZ );
    
  lbc = linebufControl( PIXSZ=PIXSZ, PHASESZ=PHASESZ, CMDSZ=CMDSZ,
                        COORDSZ=COORDSZ, PRELOAD=PRELOAD, SHIFT=SHIFT,
                        EOL=EOL, REPLICATE=REPLICATE );

  buf = [ linebuf( PIXSZ=PIXSZ, CMDSZ=CMDSZ, ADDRSZ=ADDRSZ,
                   PRELOAD=PRELOAD, SHIFT=SHIFT,
                   EOL=EOL, REPLICATE=REPLICATE )
                             {
            xmlElement = map{ "name"->"Note",
                              "attr" -> map{ "kind" -> "Directive", "context" -> "Actor", "name" -> "tokenoutputstyle" },
                              "children" -> [ map { "expr" -> "blocking"} ]
                            };
          } : for i in 0 .. (NTAPS - 1) ];

  vf  = Filter( PIXSZ=PIXSZ, PHASESZ=PHASESZ, DATASZ= DATASZ,
                COEFFSZ=COEFFSZ, GUARDBITS=GUARDBITS );
 
  chop = chop( SIZE = 2 * PIXSZ );
                   
structure
    
  IN --> lbc.IN   { bufferSize = lineBufSize; };
      
  for i in 0 .. (NTAPS - 1) do
    if i = 0 then
        
      lbc.OUT --> buf[0].IN   { bufferSize = regBufSize; }; 
      lbc.CMD --> buf[0].CMD  { bufferSize = regBufSize; }; 
          
    else
        
      buf[i - 1].OUT  --> buf[i].IN   { bufferSize = regBufSize; };
      buf[i - 1].OCMD --> buf[i].CMD  { bufferSize = regBufSize; };
          
     end
  end

  PARAM --> pc.PARAM         { bufferSize = defaultBufSize; };
  
  pc.TARGET --> lbc.TARGET   { bufferSize = defaultBufSize; };
  pc.A      --> lbc.AIN      { bufferSize = defaultBufSize; };

  // buffer size was 0
  buf[0].X --> vf.X0         { bufferSize = regOutLong; };
  buf[1].X --> vf.X1         { bufferSize = regOutLong; };
  buf[2].X --> vf.X2         { bufferSize = regOutLong; };
  buf[3].X --> vf.X3         { bufferSize = regOutLong; };
  buf[4].X --> vf.X4         { bufferSize = regOutShort; };
  buf[5].X --> vf.X5         { bufferSize = regOutShort; };
  buf[6].X --> vf.X6         { bufferSize = regOutShort; };

  WA --> vf.WA               { bufferSize = defaultBufSize; };
  WD --> vf.WD               { bufferSize = defaultBufSize; };

  lbc.AOUT --> vf.RA         { bufferSize = ctrlBufSize; };
  // lbc.USE  --> vf.USE        { bufferSize = ctrlBufSize; };
  // lbc.SOF  --> vf.SOF        { bufferSize = ctrlBufSize; };

  vf.OUT    --> chop.IN         { bufferSize = defaultBufSize; };
  chop.OUT  --> OUT             { bufferSize = defaultBufSize; };
  lbc.USE   --> chop.USE        { bufferSize = ctrlBufSize; };
  lbc.SOF   --> chop.SOF        { bufferSize = ctrlBufSize; };
  
end