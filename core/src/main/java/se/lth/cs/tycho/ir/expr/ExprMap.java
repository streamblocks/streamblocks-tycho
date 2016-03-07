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
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExprMap extends Expression {
	private ImmutableList<ImmutableEntry<Expression, Expression>> mappings;

	public ExprMap(List<? extends Map.Entry<Expression, Expression>> mappings) {
		this(null, mappings);
	}

	private ExprMap(ExprMap original, List<? extends Map.Entry<Expression, Expression>> mappings) {
		super(original);
		this.mappings = ImmutableList.from(mappings).map(ImmutableEntry::copyOf);
	}

	public ImmutableList<ImmutableEntry<Expression, Expression>> getMappings() {
		return mappings;
	}

	public ExprMap withMappings(List<? extends Map.Entry<Expression, Expression>> mappings) {
		if (Lists.sameElements(this.mappings, mappings)) {
			return this;
		} else {
			return new ExprMap(this, mappings);
		}
	}

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprMap(this, p);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		mappings.forEach(entry -> {
			action.accept(entry.getKey());
			action.accept(entry.getValue());
		});
	}

	@Override
	public ExprMap transformChildren(Transformation transformation) {
		return withMappings(
				mappings.map(entry -> ImmutableEntry.of(
						transformation.applyChecked(Expression.class, entry.getKey()),
						transformation.applyChecked(Expression.class, entry.getValue()))));
	}
}
