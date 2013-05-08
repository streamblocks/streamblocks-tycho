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

// HResizer.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

network HResizer
(
  int PIXSZ,
  int PHASESZ,
  int COORDSZ,
  int DATASZ,
  int COEFFSZ,
  int GUARDBITS
)
  IN, PARAM, WA, WD ==> OUT :
  
var
/*  
  int PIXSZ = 8;
  int PHASESZ = 8;
  int COORDSZ = 12;
  int DATASZ = 16;
  int COEFFSZ = 16;
  int GUARDBITS = 1;
*/  
  int CMDSZ   =  2;
  int PRELOAD =  1;
  int SHIFT   =  2;
  int EOF     = -1;
  int EOL     = -2;
    
  int NTAPS   =  7;

  int FUDGE = 2;  
  int regBufSize     = 4 * FUDGE;
  int lineBufSize    = 2048;
  int defaultBufSize = 4 * FUDGE;
  int regOutShort    = 8 * FUDGE;
  int regOutLong     = 8 * FUDGE;
  int ctrlBufSize    = 16 * FUDGE;

entities
      
  pc  = horizPhaseCounter( PHASESZ=PHASESZ, COORDSZ=COORDSZ, EOF=EOF, EOL=EOL );

  up  = chromaUpsample( PIXSZ=PIXSZ );

  reg = [ if i = 0 then
            registerControl( PIXSZ=PIXSZ, PHASESZ=PHASESZ, COORDSZ=COORDSZ,
                             EOF=EOF, EOL=EOL, CMDSZ=CMDSZ, 
                             PRELOAD=PRELOAD, SHIFT=SHIFT )
          else
            register( PIXSZ=PIXSZ, CMDSZ=CMDSZ, PRELOAD=PRELOAD, SHIFT=SHIFT )
          end
                 : for i in 0 .. (NTAPS - 1) ];

  hf  = Filter( PIXSZ=PIXSZ, PHASESZ=PHASESZ, DATASZ= DATASZ,
                COEFFSZ=COEFFSZ, GUARDBITS=GUARDBITS  );

  chop = chop( SIZE = 2 * PIXSZ );
  
  u = [ unpack444( PIXSZ=PIXSZ ) : for i in 1 .. NTAPS ];
  
structure
  
  PARAM --> pc.PARAM             { bufferSize = defaultBufSize; };

  IN --> up.IN                   { bufferSize = lineBufSize; };

  pc.TARGET --> reg[0].TARGET    { bufferSize = regBufSize; };
  pc.A      --> reg[0].AIN       { bufferSize = regBufSize; };
        
  for i in 0 .. (NTAPS - 1) do
    if i = 0 then
        
      up.OUT --> reg[0].IN       { bufferSize = defaultBufSize; }; 
          
    else
        
      reg[i - 1].OUT  --> reg[i].IN   { bufferSize = regBufSize; };
      reg[i - 1].OCMD --> reg[i].CMD  { bufferSize = regBufSize; };
          
    end

  reg[i].X   --> u[i].IN       { bufferSize = regOutLong; };
    
  reg[0].USE  --> u[i].USE   { bufferSize = ctrlBufSize; };
  reg[0].SOF  --> u[i].SOF   { bufferSize = ctrlBufSize; };
  
  end

  u[0].OUT   --> hf.X0         { bufferSize = regOutLong; };
  u[1].OUT   --> hf.X1         { bufferSize = regOutLong; };
  u[2].OUT   --> hf.X2         { bufferSize = regOutLong; };
  u[3].OUT   --> hf.X3         { bufferSize = regOutLong; };
  u[4].OUT   --> hf.X4         { bufferSize = regOutLong; };
  u[5].OUT   --> hf.X5         { bufferSize = regOutLong; };
  u[6].OUT   --> hf.X6         { bufferSize = regOutLong; };


  hf.OUT      --> chop.IN    { bufferSize = defaultBufSize; };
  chop.OUT    --> OUT        { bufferSize = defaultBufSize; };
  reg[0].USE  --> chop.USE   { bufferSize = ctrlBufSize; };
  reg[0].SOF  --> chop.SOF   { bufferSize = ctrlBufSize; };

  WA --> hf.WA               { bufferSize = defaultBufSize; };
  WD --> hf.WD               { bufferSize = defaultBufSize; };

  reg[0].AOUT --> hf.RA      { bufferSize = ctrlBufSize; };
  // reg[0].USE  --> hf.USE     { bufferSize = ctrlBufSize; };
  // reg[0].SOF  --> hf.SOF     { bufferSize = ctrlBufSize; };

end