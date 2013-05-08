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

// Filter.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

// Horizontal filter for resizing

network Filter
(

   int PIXSZ,
   int PHASESZ,
   int DATASZ,
   int COEFFSZ,
   int GUARDBITS // ,
   // bool HORIZONTAL

)   X0, X1, X2, X3, X4, X5, X6, /* USE, SOF, */ WD, WA, RA ==> OUT :

var

/* 
  int PIXSZ = 8;
  int PHASESZ = 8;
  int DATASZ = 16;
  int COEFFSZ = 16;
  int GUARDBITS = 1;
  bool HORIZONTAL = true;
*/  
  int NTAPS = 7;
/*    
  function configOpt( name, value ) :
    map { "name" -> "config_option", 
          "attr" -> map{ "name" -> name, "value" -> 1 } }
  end
    
  function xlimTag( value ) :
    map { "name" -> "parameter",
          "attr" -> map{ "name" -> "xlim_tag" },
          "children" -> value }
  end

  function directive( context, value ) :
    map { "name" -> "Directive",
          "attr" -> map{ "context" -> context },
          "children" -> value }
  end
*/    
  int FUDGE = 1;
  int coeffBufSize = 16;
  int defaultBufSize = 2 * FUDGE;
  int multBufSize = 16;
  int doublerBufSize = 4;
  
entities

  doubler = [ doubler( SIZE=COEFFSZ )
              {
                clockDomain="CLK_200MHz";
              } :  for i in 1 .. NTAPS ];
   
  coeff = coeff( COEFFSZ=COEFFSZ, PHASESZ=PHASESZ )
          {
            xmlElement = map{ "name"->"Note",
                              "attr" -> map{ "kind" -> "Directive", "context" -> "Actor", "name" -> "tokenoutputstyle" },
                              "children" -> [ map { "expr" -> "blocking"} ]
                            };
          }; 
    
  unpacker = [ unpack( PIXSZ=PIXSZ )
               {
                 clockDomain="CLK_200MHz";
               }  : for i in 1 .. NTAPS ];
    
  clip = clip( DATASZ=DATASZ, PIXSZ=PIXSZ, GUARDBITS=GUARDBITS )
         {
           clockDomain="CLK_200MHz";
         };
    
  repack = repack( PIXSZ=PIXSZ )
           {
              clockDomain="CLK_200MHz";
           };

    
  filter7 = Filter7( DATASZ=DATASZ, COEFFSZ=COEFFSZ, PIXSZ=PIXSZ, GUARDBITS=GUARDBITS )
            { vhdlEntity = "work.Filter7( RTL )"; clockDomain="CLK_200MHz"; };

structure

  WA --> coeff.WA                   { bufferSize = defaultBufSize; };
  WD --> coeff.WD                   { bufferSize = defaultBufSize; };
    
  RA --> coeff.RA                   { bufferSize = coeffBufSize; };
    
  X0 --> unpacker[0].IN             { bufferSize = defaultBufSize; };
  X1 --> unpacker[1].IN             { bufferSize = defaultBufSize; };
  X2 --> unpacker[2].IN             { bufferSize = defaultBufSize; };
  X3 --> unpacker[3].IN             { bufferSize = defaultBufSize; };
  X4 --> unpacker[4].IN             { bufferSize = defaultBufSize; };
  X5 --> unpacker[5].IN             { bufferSize = defaultBufSize; };
  X6 --> unpacker[6].IN             { bufferSize = defaultBufSize; };
    
  coeff.C0       --> doubler[0].IN        { bufferSize = doublerBufSize; };
  doubler[0].OUT --> filter7.C0            { bufferSize = coeffBufSize; };
  coeff.C1       --> doubler[1].IN        { bufferSize = doublerBufSize; };
  doubler[1].OUT --> filter7.C1            { bufferSize = coeffBufSize; };
  coeff.C2       --> doubler[2].IN        { bufferSize = doublerBufSize; };
  doubler[2].OUT --> filter7.C2            { bufferSize = coeffBufSize; };
  coeff.C3       --> doubler[3].IN        { bufferSize = doublerBufSize; };
  doubler[3].OUT --> filter7.C3            { bufferSize = coeffBufSize; };
  coeff.C4       --> doubler[4].IN        { bufferSize = doublerBufSize; };
  doubler[4].OUT --> filter7.C4            { bufferSize = coeffBufSize; };
  coeff.C5       --> doubler[5].IN        { bufferSize = doublerBufSize; };
  doubler[5].OUT --> filter7.C5            { bufferSize = coeffBufSize; };
  coeff.C6       --> doubler[6].IN        { bufferSize = doublerBufSize; };
  doubler[6].OUT --> filter7.C6            { bufferSize = coeffBufSize; };
    
  unpacker[0].OUT --> filter7.X0  { bufferSize = defaultBufSize; };
  unpacker[1].OUT --> filter7.X1  { bufferSize = defaultBufSize; };
  unpacker[2].OUT --> filter7.X2  { bufferSize = defaultBufSize; };
  unpacker[3].OUT --> filter7.X3  { bufferSize = defaultBufSize; };
  unpacker[4].OUT --> filter7.X4  { bufferSize = defaultBufSize; };
  unpacker[5].OUT --> filter7.X5  { bufferSize = defaultBufSize; };
  unpacker[6].OUT --> filter7.X6  { bufferSize = defaultBufSize; };

  filter7.OUT --> clip.IN            { bufferSize = multBufSize; };
  clip.OUT --> repack.IN            { bufferSize = defaultBufSize; };
    
  repack.OUT --> OUT                  { bufferSize = defaultBufSize; };

end