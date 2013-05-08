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

// bigFifo.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

// FIFO using external DRAM

network bigFifo
(
  int ADDRSZ,      // number of address bits
  int DATASZ,      // size of the data token in bytes (1,2 or 4)
  int BURSTSZ,     // burst size in data tokens - DATASZ * BURSTSZ must be divisible by 16
  int BUFSZ,       // number of 32-bit words allocated for buffer
  int BUFSTART     // buffer start address in units of 32-bit words
)   IN, RACK, WACK, RDATA ==> RADDR, WADDR, WDATA, OUT :
  
entities

  arbiter = fifoArbiter
           ( ADDRSZ = ADDRSZ,
             DATASZ = DATASZ,
             BURSTSZ = BURSTSZ,
             BUFSZ = BUFSZ,
             BUFSTART = BUFSTART
           );

  input = fifoData
          ( 
            DATASZ = DATASZ,
            BURSTSZ = BURSTSZ,
            INPUT = true
          );

  output = fifoData
          ( 
            DATASZ = DATASZ,
            BURSTSZ = BURSTSZ,
            INPUT = false
          );

structure

  IN              --> input.IN             { bufferSize = 1; };
  input.OUT       --> WDATA                { bufferSize = 1; };
  input.REQ       --> arbiter.WREQ         { bufferSize = 2; };
  
  RACK            --> arbiter.RACK         { bufferSize = 1; };
  arbiter.RADDR   --> RADDR                { bufferSize = 1; };
  arbiter.WADDR   --> WADDR                { bufferSize = 1; };
  WACK            --> arbiter.WACK         { bufferSize = 1; };
  
  output.REQ      --> arbiter.RREQ         { bufferSize = 2; };
  RDATA           --> output.IN            { bufferSize = 1; };
  output.OUT      --> OUT;
  
end