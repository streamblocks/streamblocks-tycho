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

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Jorn W. Janneck <jwj@acm.org>
 */

public class ExprMap extends Expression {
	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprMap(this, p);
	}

	public ExprMap(ImmutableList<ImmutableEntry<Expression, Expression>> mappings,
			ImmutableList<GeneratorFilter> generators) {
		this(null, mappings, generators);
	}

	private ExprMap(ExprMap original, ImmutableList<ImmutableEntry<Expression, Expression>> mappings,
			ImmutableList<GeneratorFilter> generators) {
		super(original);
		this.mappings = ImmutableList.from(mappings);
		this.generators = ImmutableList.from(generators);
	}

	public ExprMap copy(ImmutableList<ImmutableEntry<Expression, Expression>> mappings,
			ImmutableList<GeneratorFilter> generators) {
		if (Lists.equals(this.mappings, mappings) && Lists.equals(this.generators, generators)) {
			return this;
		}
		return new ExprMap(this, mappings, generators);
	}

	public ExprMap(ImmutableList<ImmutableEntry<Expression, Expression>> mappings) {
		this(mappings, null);
	}

	public ImmutableList<ImmutableEntry<Expression, Expression>> getMappings() {
		return mappings;
	}

	public ImmutableList<GeneratorFilter> getGenerators() {
		return generators;
	}

	private ImmutableList<ImmutableEntry<Expression, Expression>> mappings;
	private ImmutableList<GeneratorFilter> generators;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		mappings.forEach(entry -> {
			action.accept(entry.getKey());
			action.accept(entry.getValue());
		});
		generators.forEach(action);
	}

	@Override
	public ExprMap transformChildren(Function<? super IRNode, ? extends IRNode> transformation) {
		Function<? super ImmutableEntry<Expression, Expression>, ? extends ImmutableEntry<Expression, Expression>> function = (ImmutableEntry<Expression, Expression> entry) -> {
			Expression key = (Expression) transformation.apply(entry.getKey());
			Expression value = (Expression) transformation.apply(entry.getValue());
			if (key == entry.getKey() && value == entry.getValue()) {
				return entry;
			} else {
				return ImmutableEntry.of(key, value);
			}
		};
		return copy(
				mappings.map(function),
				(ImmutableList) generators.map(transformation)
		);
	}
}
