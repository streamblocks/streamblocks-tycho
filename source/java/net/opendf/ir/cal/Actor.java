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

import java.util.List;

import net.opendf.ir.common.CompositePortDecl;
import net.opendf.ir.common.DeclEntity;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.NamespaceDecl;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.QID;


public class Actor extends DeclEntity  {

	    
	public Actor(String name, NamespaceDecl namespace,
			ParDeclType [] typePars, ParDeclValue [] valuePars, DeclType [] typeDecls, DeclVar [] varDecls,
        CompositePortDecl inputPorts, CompositePortDecl outputPorts,
        Action [] initializers, Action [] actions, 
        ScheduleFSM scheduleFSM, List<QID> [] priorities, Expression [] invariants)
    {
		super(name, namespace, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts);
        
        this.initializers = initializers;
        this.actions = actions;
        this.scheduleFSM = scheduleFSM;
        this.priorities = priorities;
        this.invariants = invariants;
    }
		

	

    public Action [] getActions() {
        return actions;
    }

    public Action [] getInitializers() {
        return initializers;
    }

    public Expression [] getInvariants() {
        return invariants;
    }

    public ScheduleFSM getScheduleFSM() {
    	return scheduleFSM;
    }

    public List<QID> [] getPriorities() {
    	return priorities;
    }

 
    private Action []       actions;
    private ScheduleFSM     scheduleFSM;
    private List<QID> [] 	priorities;
    private Expression []   invariants;


    private Action []       initializers;
}
