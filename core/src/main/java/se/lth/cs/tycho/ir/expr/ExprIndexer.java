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

package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Jorn W. Janneck
 */

public class ExprIndexer extends Expression {

	public ExprIndexer(Expression structure, Expression index) {
		this(null, structure, index);
	}

	private ExprIndexer(ExprIndexer original, Expression structure, Expression index) {
		super(original);
		this.structure = structure;
		this.index = index;
	}

	public ExprIndexer copy(Expression structure, Expression index) {
		if (Objects.equals(this.structure, structure) && Objects.equals(this.index, index)) {
			return this;
		}
		return new ExprIndexer(this, structure, index);
	}

	public Expression getStructure() {
		return structure;
	}

	public Expression getIndex() {
		return index;
	}

	private Expression structure;

	private Expression index;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(structure);
		action.accept(index);
	}

	@Override
	public ExprIndexer transformChildren(Transformation transformation) {
		return copy(
				(Expression) transformation.apply(structure),
				(Expression) transformation.apply(index)
		);
	}
}
