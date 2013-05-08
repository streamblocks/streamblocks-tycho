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

// Resizer.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

network Resizer
(
/* 
  int PIXSZ,
  int COORDSZ,
  int COEFFSZ
  */
)
  IN, HPARAM, VPARAM, WA, WD ==> OUT :
  
var
 
  int PIXSZ = 8;
  int COORDSZ = 12;
  int COEFFSZ = 16;

  
  int PHASESZ   = 8;
  int ADDRSZ    = 11;
  int DATASZ    = 16;
  int GUARDBITS = 1;

entities

  h = HResizer( PIXSZ=PIXSZ, PHASESZ=PHASESZ, COORDSZ=COORDSZ,
                COEFFSZ=COEFFSZ, DATASZ= DATASZ, GUARDBITS=GUARDBITS );

  v = VResizer( PIXSZ=PIXSZ, PHASESZ=PHASESZ, COORDSZ=COORDSZ,
                COEFFSZ=COEFFSZ, ADDRSZ=ADDRSZ,
                DATASZ= DATASZ, GUARDBITS=GUARDBITS );

structure

  HPARAM --> h.PARAM   { bufferSize = 2; };  
  WA     --> h.WA      { bufferSize = 2; };
  WD     --> h.WD      { bufferSize = 2; };

  VPARAM --> v.PARAM   { bufferSize = 2; };  
  WA     --> v.WA      { bufferSize = 2; };
  WD     --> v.WD      { bufferSize = 2; };
 
  IN     --> h.IN      { bufferSize = 2048 /*16 */; };
  h.OUT  --> v.IN      { bufferSize = 2048 /* 32768 */; };
  v.OUT  --> OUT       { bufferSize = 16; };

end