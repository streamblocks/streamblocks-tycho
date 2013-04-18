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

import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public class ExprList extends Expression {
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprList(this, p);
	}

	public ExprList(ImmutableList<Expression> elements, ImmutableList<GeneratorFilter> generators) {
		this(null, elements, generators);
	}

	public ExprList(ImmutableList<Expression> elements) {
		this(null, elements, null);
	}

	private ExprList(ExprList original, ImmutableList<Expression> elements, ImmutableList<GeneratorFilter> generators) {
		super(original);
		this.elements = ImmutableList.copyOf(elements);
		this.generators = ImmutableList.copyOf(generators);
	}

	public ExprList copy(ImmutableList<Expression> elements, ImmutableList<GeneratorFilter> generators) {
		if (Lists.equals(this.elements, elements) && Lists.equals(this.generators, generators)) {
			return this;
		}
		return new ExprList(this, elements, generators);
	}

	public ImmutableList<Expression> getElements() {
		return elements;
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	private ImmutableList<Expression> elements;
	private ImmutableList<GeneratorFilter> generators;
}
