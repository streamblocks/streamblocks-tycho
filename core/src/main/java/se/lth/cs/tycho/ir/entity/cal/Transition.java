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

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

/**
 * 
 * @author jornj
 */
public class Transition extends AbstractIRNode {

	public Transition(String sourceState, String destinationState, ImmutableList<QID> actionTags) {
		this(null, sourceState, destinationState, actionTags);
	}

	private Transition(Transition original, String sourceState, String destinationState, ImmutableList<QID> actionTags) {
		super(original);
		this.sourceState = sourceState;
		this.destinationState = destinationState;
		this.actionTags = ImmutableList.from(actionTags);
	}

	public Transition copy(String sourceState, String destinationState, ImmutableList<QID> actionTags) {
		if (Objects.equals(this.sourceState, sourceState) && Objects.equals(this.destinationState, destinationState)
				& Lists.equals(this.actionTags, actionTags)) {
			return this;
		}
		return new Transition(this, sourceState, destinationState, actionTags);
	}

	/**
	 * @return Returns the actionTags.
	 */
	public ImmutableList<QID> getActionTags() {
		return actionTags;
	}

	/**
	 * @return Returns the destinationState.
	 */
	public String getDestinationState() {
		return destinationState;
	}

	/**
	 * @return Returns the sourceState.
	 */
	public String getSourceState() {
		return sourceState;
	}

	private String sourceState;
	private String destinationState;
	private ImmutableList<QID> actionTags;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public Transition transformChildren(Transformation transformation) {
		return this;
	}
}
