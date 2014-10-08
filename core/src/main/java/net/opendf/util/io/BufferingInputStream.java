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

package net.opendf.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BufferingInputStream extends InputStream {
	
	//
	//  InputStream
	//

	@Override
	public int read() throws IOException {
		while (currentBuffer < buffers.size()) {
			byte[] buf = buffers.get(currentBuffer);
			if (currentPos < buf.length) {
				byte b = buf[currentPos++];
				if (b >= 0) 
					return (int)b;
				else 
					return 256 + (int)b;
			}
			currentBuffer += 1;
			currentPos = 0;
		}
		return -1;
	}
	
	//
	//  BufferingInputStream
	//
	
	public void  resetToStart() {
		currentBuffer = 0;
		currentPos = 0;
	}
	
	public BufferingInputStream() {
	}
	
	public BufferingInputStream(InputStream s) throws IOException {
		this(s, DefaultChunkSize);
	}
	
	public BufferingInputStream(InputStream s, int chunkSize) throws IOException {
		addInputStream(s, chunkSize);
	}
	

	//
	//  private
	//
	
	private void  addInputStream(InputStream s, int chunkSize) throws IOException {
		boolean done = false;
		while (!done) {
			byte [] buf = new byte[chunkSize];
			int n = s.read(buf);
			if (n > 0) {
				if (n < buf.length) {
					done = true;
					byte [] buf2 = new byte [n];
					System.arraycopy(buf, 0, buf2, 0, n);
					buf = buf2;
				}
				addBuffer(buf);
			} else {
				done = true;
			}
		}
	}

	
	private void  addBuffer(byte [] buf) {
		buffers.add(buf);
	}
	
	private final static int DefaultChunkSize = 4096; 
	
	private List<byte []> buffers = new ArrayList<byte[]>();
	
	private int currentBuffer = 0;
	private int currentPos = 0;

}
