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

package net.opendf.ir.cal;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.QID;
import net.opendf.ir.common.Statement;


public class Action extends AbstractIRNode {

    public Action(int id, QID tag,
                  InputPattern [] inputPatterns, OutputExpression [] outputExpressions,
                  DeclType [] typeDecls, DeclVar[] varDecls, Expression [] guards, Statement [] body, Expression delay,
                  Expression [] preconditions, Expression [] postconditions) {
    	this.id = id;
        this.tag = tag;
        this.inputPatterns = inputPatterns;
        this.outputExpressions = outputExpressions;
        this.typeDecls = typeDecls;
        this.varDecls = varDecls;
        this.guards = guards;
        this.body = body;
        this.delay = delay;
        this.preconditions = preconditions;
        this.postconditions = postconditions;
    }

    public QID getTag() {
        return tag;
    }

    public InputPattern[] getInputPatterns() {
        return inputPatterns;
    }

    public OutputExpression[] getOutputExpressions() {
        return outputExpressions;
    }

    public DeclType [] getTypeDecls() {
        return typeDecls;
    }

    public DeclVar [] getVarDecls() {
        return varDecls;
    }

    public Expression[] getGuards() {
        return guards;
    }

    public Statement[] getBody() {
        return body;
    }
    
    public Expression  getDelay() {
    	return delay;
    }
    
    public int  getID() {
    	return id;
    }
    
    public Expression []  getPreconditions() {
    	return preconditions;
    }
    
    public Expression []  getPostconditions() {
    	return postconditions;
    }

    private int                 id;
    private QID					tag;
    private InputPattern []     inputPatterns;
    private OutputExpression [] outputExpressions;
    private DeclType []			typeDecls;
    private DeclVar [] 			varDecls;
    private Expression []       guards;
    private Statement []        body;
    private Expression          delay;
    private Expression []       preconditions;
    private Expression []       postconditions;


    public String toString() {
    	return "(Action " + tag + ")";
//        Utility.increaseTabDepth(2);
//        String tabs = Utility.getHeadingTabs();
//        String result =  "Action " + this.tag + ":\n" + tabs + "inputPatterns:\n" +
//                Utility.arrayToString(this.inputPatterns) + "\n" + tabs + "outputExpressions:\n" +
//                Utility.arrayToString(this.outputExpressions);
//        Utility.decreaseTabDepth(2);
//        return result;
    }
}
