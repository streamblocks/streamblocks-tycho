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

package se.lth.cs.tycho.ir.entity.cal;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class Action extends AbstractIRNode {

    public Action(QID tag, ImmutableList<Annotation> annotations, ImmutableList<InputPattern> inputPatterns,
                  ImmutableList<OutputExpression> outputExpressions, ImmutableList<TypeDecl> typeDecls,
                  ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
                  Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
        this(null, tag, annotations, inputPatterns, outputExpressions, typeDecls, varDecls, guards, body, delay, preconditions,
                postconditions);
    }

    private Action(Action original, QID tag, ImmutableList<Annotation> annotations, ImmutableList<InputPattern> inputPatterns,
                   ImmutableList<OutputExpression> outputExpressions, ImmutableList<TypeDecl> typeDecls,
                   ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
                   Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
        super(original);
        this.tag = tag;
        this.annotations =  ImmutableList.from(annotations);
        this.inputPatterns = ImmutableList.from(inputPatterns);
        this.outputExpressions = ImmutableList.from(outputExpressions);
        this.typeDecls = ImmutableList.from(typeDecls);
        this.varDecls = ImmutableList.from(varDecls);
        this.guards = ImmutableList.from(guards);
        this.body = ImmutableList.from(body);
        this.delay = delay;
        this.preconditions = ImmutableList.from(preconditions);
        this.postconditions = ImmutableList.from(postconditions);
    }

    public Action copy(QID tag, ImmutableList<Annotation> annotations, ImmutableList<InputPattern> inputPatterns,
                       ImmutableList<OutputExpression> outputExpressions, ImmutableList<TypeDecl> typeDecls,
                       ImmutableList<LocalVarDecl> varDecls, ImmutableList<Expression> guards, ImmutableList<Statement> body,
                       Expression delay, ImmutableList<Expression> preconditions, ImmutableList<Expression> postconditions) {
        if (Objects.equals(this.tag, tag) && Lists.equals(this.annotations, annotations) && Lists.equals(this.inputPatterns, inputPatterns)
                && Lists.equals(this.outputExpressions, outputExpressions) && Lists.equals(this.typeDecls, typeDecls)
                && Lists.equals(this.varDecls, varDecls) && Lists.equals(this.guards, guards)
                && Lists.equals(this.body, body) && Objects.equals(this.delay, delay)
                && Lists.equals(this.preconditions, preconditions) && Lists.equals(this.postconditions, postconditions)) {
            return this;
        }
        return new Action(this, tag, annotations, inputPatterns, outputExpressions, typeDecls, varDecls, guards, body, delay,
                preconditions, postconditions);
    }

    public QID getTag() {
        return tag;
    }

    public ImmutableList<InputPattern> getInputPatterns() {
        return inputPatterns;
    }

    public ImmutableList<OutputExpression> getOutputExpressions() {
        return outputExpressions;
    }

    public ImmutableList<TypeDecl> getTypeDecls() {
        return typeDecls;
    }

    public ImmutableList<LocalVarDecl> getVarDecls() {
        return varDecls;
    }

    public ImmutableList<Expression> getGuards() {
        return guards;
    }

    public ImmutableList<Annotation> getAnnotations() {return annotations;}

    public Action withGuards(List<Expression> guards) {
        if (Lists.sameElements(this.guards, guards)) {
            return this;
        } else {
            return new Action(this, tag, annotations, inputPatterns, outputExpressions, typeDecls, varDecls, ImmutableList.from(guards), body, delay, preconditions, postconditions);
        }
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public Expression getDelay() {
        return delay;
    }

    public ImmutableList<Expression> getPreconditions() {
        return preconditions;
    }

    public ImmutableList<Expression> getPostconditions() {
        return postconditions;
    }

    public Action withVarDecls(List<LocalVarDecl> varDecls) {
        if (Lists.sameElements(this.varDecls, varDecls)) {
            return this;
        } else {
            return new Action(this, tag, annotations, inputPatterns, outputExpressions, typeDecls, ImmutableList.from(varDecls), guards, body, delay, preconditions, postconditions);
        }
    }

    public Action withInputPatterns(List<InputPattern> inputPatterns) {
        if (Lists.sameElements(this.inputPatterns, inputPatterns)) {
            return this;
        } else {
            return new Action(this, tag, annotations, ImmutableList.from(inputPatterns), outputExpressions, typeDecls, varDecls, guards, body, delay, preconditions, postconditions);
        }
    }

    public Action withOutputExpressions(List<OutputExpression> outputExpressions) {
        if (Lists.sameElements(this.outputExpressions, outputExpressions)) {
            return this;
        } else {
            return new Action(this, tag, annotations, inputPatterns, ImmutableList.from(outputExpressions), typeDecls, varDecls, guards, body, delay, preconditions, postconditions);
        }
    }

    private QID tag;
    private ImmutableList<Annotation> annotations;
    private ImmutableList<InputPattern> inputPatterns;
    private ImmutableList<OutputExpression> outputExpressions;
    private ImmutableList<TypeDecl> typeDecls;
    private ImmutableList<LocalVarDecl> varDecls;
    private ImmutableList<Expression> guards;
    private ImmutableList<Statement> body;
    private Expression delay;
    private ImmutableList<Expression> preconditions;
    private ImmutableList<Expression> postconditions;

    public String toString() {
        return "(Action " + tag + ")";
        // Utility.increaseTabDepth(2);
        // String tabs = Utility.getHeadingTabs();
        // String result = "Action " + this.tag + ":\n" + tabs +
        // "inputPatterns:\n" +
        // Utility.arrayToString(this.inputPatterns) + "\n" + tabs +
        // "outputExpressions:\n" +
        // Utility.arrayToString(this.outputExpressions);
        // Utility.decreaseTabDepth(2);
        // return result;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        inputPatterns.forEach(action);
        outputExpressions.forEach(action);
        typeDecls.forEach(action);
        varDecls.forEach(action);
        guards.forEach(action);
        body.forEach(action);
        if (delay != null) action.accept(delay);
        preconditions.forEach(action);
        postconditions.forEach(action);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Action transformChildren(Transformation transformation) {
        return copy(
                tag,
                (ImmutableList) annotations.map(transformation),
                (ImmutableList) inputPatterns.map(transformation),
                (ImmutableList) outputExpressions.map(transformation),
                (ImmutableList) typeDecls.map(transformation),
                (ImmutableList) varDecls.map(transformation),
                (ImmutableList) guards.map(transformation),
                (ImmutableList) body.map(transformation),
                delay == null ? null : (Expression) transformation.apply(delay),
                (ImmutableList) preconditions.map(transformation),
                (ImmutableList) postconditions.map(transformation)
        );
    }

    public Action withTag(QID tag) {
        if (Objects.equals(this.tag, tag)) {
            return this;
        } else {
            return new Action(this, tag, annotations, inputPatterns, outputExpressions, typeDecls, varDecls, guards, body, delay, preconditions, postconditions);
        }
    }

    /**
     * Apply transformation to the port array indexes and repeat expressions in the input patterns and output
     * expressions only.
     *
     * @param transformation Transformation to apply.
     * @return New actor with transformed port array indexes and repeat expressions.
     */
    public Action transformPortArraysAndRepeats(Transformation transformation) {
        ImmutableList.Builder<InputPattern> inputPatternBuilder = ImmutableList.builder();
        ImmutableList.Builder<OutputExpression> outputExpressionBuilder = ImmutableList.builder();

        for (InputPattern inputPattern: inputPatterns) {
            inputPatternBuilder.add(inputPattern.transformPortArrayAndRepeat(transformation));
        }
        for (OutputExpression outputExpression: outputExpressions) {
            outputExpressionBuilder.add(outputExpression.transformPortArrayAndRepeat(transformation));
        }

        return new Action(this, tag, annotations, inputPatternBuilder.build(), outputExpressionBuilder.build(), typeDecls, varDecls, guards, body, delay, preconditions, postconditions);
    }
}
