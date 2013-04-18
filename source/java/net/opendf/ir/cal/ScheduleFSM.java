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

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

/**
 * 
 * @author jornj
 */

public class ScheduleFSM extends AbstractIRNode {

	public ScheduleFSM(ImmutableList<Transition> transitions, String initialState) {
		this(null, transitions, initialState);
	}

	private ScheduleFSM(ScheduleFSM original, ImmutableList<Transition> transitions, String initialState) {
		super(original);
		this.transitions = ImmutableList.copyOf(transitions);
		this.initialState = initialState;
	}
	
	public ScheduleFSM copy(ImmutableList<Transition> transitions, String initialState) {
		if (Lists.equals(this.transitions, transitions) && Objects.equals(this.initialState, initialState)) {
			return this;
		}
		return new ScheduleFSM(this, transitions, initialState);
	}


	public String getInitialState() {
		return initialState;
	}

	public ImmutableList<Transition> getTransitions() {
		return transitions;
	}

	private ImmutableList<Transition> transitions;
	private String initialState;

}
