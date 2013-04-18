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

import java.util.Objects;

import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class ExprLet extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprLet(this, p);
	}

	public ExprLet(ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls, Expression body) {
		this(null, typeDecls, varDecls, body);
	}

	private ExprLet(ExprLet original, ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls,
			Expression body) {
		super(original);
		this.body = body;
		this.typeDecls = ImmutableList.copyOf(typeDecls);
		this.varDecls = ImmutableList.copyOf(varDecls);
	}

	public ExprLet copy(ImmutableList<DeclType> typeDecls, ImmutableList<DeclVar> varDecls, Expression body) {
		if (Lists.equals(this.typeDecls, typeDecls) && Lists.equals(this.varDecls, varDecls)
				&& Objects.equals(this.body, body)) {
			return this;
		}
		return new ExprLet(this, typeDecls, varDecls, body);
	}

	public ImmutableList<DeclType> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<DeclVar> getVarDecls() {
		return varDecls;
	}

	public Expression getBody() {
		return body;
	}

	private ImmutableList<DeclType> typeDecls;
	private ImmutableList<DeclVar> varDecls;
	private Expression body;
}
