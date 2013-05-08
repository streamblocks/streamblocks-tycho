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

// Hobson.nl
//
// Author: David B. Parlour (dave.parlour@xilinx.com)
//

network Hobson() IN, RACK, WACK, RDATA ==> RADDR, WADDR, WDATA, OUT :
{ partname="xc4vsx35-10-FF668";
  sdcConstraint = "define_clock {CLK}        -freq 144 -clockgroup default_clkgroup_0";
  sdcConstraint = "define_clock {CLK_100MHz} -freq 120 -clockgroup default_clkgroup_1";
  sdcConstraint = "define_clock {CLK_200MHz} -freq 240 -clockgroup default_clkgroup_2";
}
var
  int PIXSZ = 8;
  int COORDSZ = 12;
  int PHASESZ = 8;
  int COEFFSZ = 16;
  
  int IWIDTH = 1280;
  int IHEIGHT = 1024;
  int OWIDTH = 1200; // 936;
  int OHEIGHT = 960; // 768;

entities

  fifo = bigFifo ( ADDRSZ = 26, DATASZ = 2, BURSTSZ = OWIDTH, BUFSZ=8000, BUFSTART=12000 );
        
  stuffer = cmdStuffer( PIXSZ=PIXSZ, COORDSZ=COORDSZ, PHASESZ=PHASESZ, COEFFSZ=COEFFSZ,
                        IWIDTH=IWIDTH, IHEIGHT=IHEIGHT, CROP_WIDTH=OWIDTH, CROP_HEIGHT=OHEIGHT,
                        START_INDEX = 0 )
    { xmlElement = [
                    map{ "name"->"Note",
                        "attr" -> map{ "kind" -> "Directive", "context" -> "Actor", "name" -> "tokenoutputstyle" },
                        "children" -> [ map { "expr" -> "simple"} ]
                     },
                    map{ "name"->"Note",
                         "attr" -> map { "kind" -> "Directive", "context" -> "Actor", "name" -> "xlim_tag" },
                         "children" -> [ map { "name" -> "config_option",
                                               "attr" -> map{ "name" -> "project.xflow.xilinx_part" },
                                               "children"->[ "xc4vsx35-10-ff668" ]
                                             }
                                       ]
                       }
                   ];
    };

  cropper = cropper( PIXSZ=PIXSZ, COORDSZ=COORDSZ, IWIDTH=IWIDTH, IHEIGHT=IHEIGHT );

  extract = ExtractSOF( PIXSZ=PIXSZ );
  
  resizer = Resizer( PIXSZ=PIXSZ, COORDSZ=COORDSZ, COEFFSZ=COEFFSZ )
            { clockDomain = "CLK_100MHz"; };
   
  pad = pad( PIXSZ=PIXSZ, COORDSZ=COORDSZ, OWIDTH=OWIDTH, OHEIGHT=OHEIGHT );
  
sink     = hobsonTestSink( MAXW=OWIDTH, MAXH=OHEIGHT ); 
  
structure
  
  IN --> stuffer.IN  { bufferSize = 2048; };
  
  stuffer.OUT --> cropper.IN  { bufferSize = 16; };
  
  stuffer.X --> resizer.HPARAM { bufferSize = 32; };
  stuffer.Y --> resizer.VPARAM { bufferSize = 32; };

  stuffer.WA --> resizer.WA { bufferSize = 16; };
  stuffer.WD --> resizer.WD { bufferSize = 16; };

  stuffer.PAD --> pad.NEXT { bufferSize = 32; };

  cropper.OUT --> sink.IN { bufferSize = 2000; };
 
  cropper.OUT --> fifo.IN { bufferSize = 2; };
  fifo.OUT    --> extract.IN { bufferSize = 2; };
  
  RACK        --> fifo.RACK;
  WACK        --> fifo.WACK;
  RDATA       --> fifo.RDATA;
  fifo.RADDR  --> RADDR;
  fifo.WADDR  --> WADDR;
  fifo.WDATA  --> WDATA;
  
  extract.OUT --> resizer.IN { bufferSize = 2; };

  resizer.OUT --> pad.IN { bufferSize = 16; };
  
  pad.OUT --> OUT { bufferSize = 16; };

  end
  