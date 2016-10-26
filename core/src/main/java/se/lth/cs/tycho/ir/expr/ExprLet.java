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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class ExprLet extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprLet(this, p);
	}

	public ExprLet(ImmutableList<TypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls, Expression body) {
		this(null, typeDecls, varDecls, body);
	}

	private ExprLet(ExprLet original, ImmutableList<TypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls,
			Expression body) {
		super(original);
		this.body = body;
		this.typeDecls = ImmutableList.from(typeDecls);
		this.varDecls = ImmutableList.from(varDecls);
	}

	public ExprLet copy(ImmutableList<TypeDecl> typeDecls, ImmutableList<LocalVarDecl> varDecls, Expression body) {
		if (Lists.equals(this.typeDecls, typeDecls) && Lists.equals(this.varDecls, varDecls)
				&& Objects.equals(this.body, body)) {
			return this;
		}
		return new ExprLet(this, typeDecls, varDecls, body);
	}

	public ImmutableList<TypeDecl> getTypeDecls() {
		return typeDecls;
	}

	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	public Expression getBody() {
		return body;
	}

	public ExprLet withVarDecls(List<LocalVarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new ExprLet(this, typeDecls, ImmutableList.from(varDecls), body);
		}
	}

	private ImmutableList<TypeDecl> typeDecls;
	private ImmutableList<LocalVarDecl> varDecls;
	private Expression body;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		typeDecls.forEach(action);
		varDecls.forEach(action);
		action.accept(body);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExprLet transformChildren(Transformation transformation) {
		return copy(
				(ImmutableList) typeDecls.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(Expression) transformation.apply(body)
		);
	}
}
