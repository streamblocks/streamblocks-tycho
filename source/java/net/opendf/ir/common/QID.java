/* 
BEGINCOPYRIGHT X,UC
	
	Copyright (c) 2007, Xilinx Inc.
	Copyright (c) 2003, The Regents of the University of California
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
	- Neither the names of the copyright holders nor the names 
	  of contributors may be used to endorse or promote 
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

package net.opendf.ir.common;

import java.util.Arrays;

import net.opendf.ir.AbstractIRNode;

/**
 * 
 * @author jornj
 */
public class QID extends AbstractIRNode {

	public boolean isPrefixOf(QID q) {
		if (q.ids.length < this.ids.length)
			return false;

		for (int i = 0; i < this.ids.length; i++) {
			if (!this.ids[i].equals(q.ids[i]))
				return false;
		}
		return true;
	}

	public boolean isStrictPrefixOf(QID q) {
		return this.size() < q.size() && this.isPrefixOf(q);
	}

	public int size() {
		return ids.length;
	}

	public QID(String[] ids) {
		this(null, ids);
	}

	private QID(QID original, String[] ids) {
		super(original);
		this.ids = Arrays.copyOf(ids, ids.length);
	}

	public QID copy(String[] ids) {
		if (Arrays.equals(this.ids, ids)) {
			return this;
		}
		return new QID(this, ids);
	}

	private String[] ids;

	public String toString() {
		if (ids.length == 0)
			return "";
		String s = ids[0];
		for (int i = 1; i < ids.length; i++) {
			s += ".";
			s += ids[i];
		}
		return s;
	}
}
