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

package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Jorn W. Janneck
 */

public class StmtWhile extends Statement {

    public StmtWhile(List<Annotation> annotations, Expression condition, List<Statement> body) {
        this(null, annotations, condition, body);
    }

    public StmtWhile(Expression condition, List<Statement> body) {
        this(null, ImmutableList.empty(), condition, body);
    }

    private StmtWhile(StmtWhile original, List<Annotation> annotations, Expression condition, List<Statement> body) {
        super(original);
        this.annotations = ImmutableList.from(annotations);
        this.condition = condition;
        this.body = ImmutableList.from(body);
    }

    public StmtWhile copy(List<Annotation> annotations, Expression condition, List<Statement> body) {
        if (Objects.equals(this.annotations, annotations) && this.condition == condition && Lists.sameElements(this.body, body)) {
            return this;
        }
        return new StmtWhile(this, annotations, condition, body);
    }

    public Expression getCondition() {
        return condition;
    }

    public StmtWhile withCondition(Expression condition) {
        return copy(annotations, condition, body);
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public StmtWhile withBody(List<Statement> body) {
        return copy(annotations, condition, body);
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public Statement withAnnotations(List<Annotation> annotations) {
        return copy(annotations, condition, body);
    }

    private Expression condition;
    private ImmutableList<Statement> body;
    private ImmutableList<Annotation> annotations;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        action.accept(condition);
        body.forEach(action);
    }

    @Override
    public StmtWhile transformChildren(Transformation transformation) {
        return copy(
                transformation.mapChecked(Annotation.class, annotations),
                transformation.applyChecked(Expression.class, condition),
                transformation.mapChecked(Statement.class, body));
    }
}
