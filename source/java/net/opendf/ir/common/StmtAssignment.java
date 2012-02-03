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



public class StmtAssignment extends Statement {

    public void accept(StatementVisitor v) {
        v.visitStmtAssignment(this);
    }

    public StmtAssignment(String var, Expression val) {
        this(var, val, null, null);
    }

    public StmtAssignment(String var, Expression val, Expression [] location) {
    	this(var, val, location, null);
    }

    protected StmtAssignment(String var, Expression val, Expression [] location, String field) {
        this.var = var;
        this.val = val;
        this.field = field;
        this.location = location;
    }
    
    public StmtAssignment(String var, Expression val, String field) {
    	this(var, val, null, field);
    }

    public String getVar() {
        return var;
    }

    public Expression getVal() {
        return val;
    }

    public Expression[] getLocation() {
        return location;
    }
    
    public String  getField() {
    	return field;
    }

    private String      var;
    private Expression  val;
    private Expression [] location;
    private String field;
    
    
    public long  getVariableLocation() {
    	return variableLocation;
    }
    
    public void  setVariableLocation(long location) {
    	variableLocation = location;
    }
    
    private long variableLocation = -1;


}
