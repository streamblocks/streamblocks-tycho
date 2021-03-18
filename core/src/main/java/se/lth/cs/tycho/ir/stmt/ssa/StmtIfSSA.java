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

package se.lth.cs.tycho.ir.stmt.ssa;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * @author Jorn W. Janneck
 */

public class StmtIfSSA extends Statement {

    public StmtIfSSA(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch, List<Statement> join) {
        this(null, condition, thenBranch, elseBranch, join);
    }

    public StmtIfSSA(Expression condition, List<Statement> thenBranch, List<Statement> join) {
        this(condition, thenBranch, null, join);
    }

    private StmtIfSSA(StmtIfSSA original, Expression condition, List<Statement> thenBranch, List<Statement> elseBranch, List<Statement> join) {
        super(original);
        this.condition = condition;
        this.thenBranch = ImmutableList.from(thenBranch);
        this.elseBranch = ImmutableList.from(elseBranch);
        this.join = ImmutableList.from(join);
    }

    public StmtIfSSA copy(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch, List<Statement> join) {
        if (this.condition == condition && Lists.sameElements(this.thenBranch, thenBranch)
                && Lists.sameElements(this.elseBranch, elseBranch) && Lists.sameElements(this.join, join)) {
            return this;
        }
        return new StmtIfSSA(this, condition, thenBranch, elseBranch, join);
    }

    public StmtIfSSA copy(Expression condition, List<Statement> thenBranch, List<Statement> join) {
        if (Objects.equals(this.condition, condition) && Objects.equals(this.thenBranch, thenBranch)
                && this.elseBranch == null && Objects.equals(this.join, join)) {
            return this;
        }
        return new StmtIfSSA(this, condition, thenBranch, null, join);
    }

    public Expression getCondition() {
        return condition;
    }

    public StmtIfSSA withCondition(Expression condition) {
        return copy(condition, thenBranch, elseBranch, join);
    }

    public ImmutableList<Statement> getThenBranch() {
        return thenBranch;
    }

    public StmtIfSSA withThenBranch(List<Statement> thenBranch) {
        return copy(condition, thenBranch, elseBranch, join);
    }

    public ImmutableList<Statement> getElseBranch() {
        return elseBranch;
    }

    public StmtIfSSA withElseBranch(List<Statement> elseBranch) {
        return copy(condition, thenBranch, elseBranch, join);
    }

    public StmtIfSSA withJoin(List<Statement> join) {
        return copy(condition, thenBranch, elseBranch, join);
    }

    public ImmutableList<Statement> getJoin() {
        return join;
    }

    private Expression condition;
    private ImmutableList<Statement> thenBranch;
    private ImmutableList<Statement> elseBranch;
    private ImmutableList<Statement> join;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(condition);
        thenBranch.forEach(action);
        elseBranch.forEach(action);
        join.forEach(action);
    }

    @Override
    public StmtIfSSA transformChildren(Transformation transformation) {
        return copy(
                transformation.applyChecked(Expression.class, condition),
                transformation.mapChecked(Statement.class, thenBranch),
                transformation.mapChecked(Statement.class, elseBranch),
                transformation.mapChecked(Statement.class, join)
        );
    }
}
