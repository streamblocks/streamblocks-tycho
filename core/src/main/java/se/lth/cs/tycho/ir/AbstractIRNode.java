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

package se.lth.cs.tycho.ir;

/**
 * Abstract implementation of IRNode that handles line and column numbers.
 *
 * @author Christopher Chang
 * @author Jorn W. Janneck
 */
public abstract class AbstractIRNode implements IRNode {
	private int fromLineNumber = 0;
	private int fromColumnNumber = 0;
	private int toLineNumber = 0;
	private int toColumnNumber = 0;

	/**
	 * Constructs a node with the same line and column numbers as {@code original}.
	 *
	 * @param original the original node
	 */
	public AbstractIRNode(IRNode original) {
		if (original != null)
			setPosition(
					original.getFromLineNumber(),
					original.getFromColumnNumber(),
					original.getToLineNumber(),
					original.getToColumnNumber());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFromLineNumber() {
		return fromLineNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFromColumnNumber() {
		return fromColumnNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getToLineNumber() {
		return toLineNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getToColumnNumber() {
		return toColumnNumber;
	}

	/**
	 * Updates the position information.
	 *
	 * @param fromLineNumber start line number
	 * @param fromColumnNumber start column number
	 * @param toLineNumber end line number
	 * @param toColumnNumber end column number
	 */
	public void setPosition(int fromLineNumber, int fromColumnNumber, int toLineNumber, int toColumnNumber) {
		this.fromLineNumber = fromLineNumber;
		this.fromColumnNumber = fromColumnNumber;
		this.toLineNumber = toLineNumber;
		this.toColumnNumber = toColumnNumber;
	}

	/**
	 * Set the position of this from the position given by the {@code node}
	 * @param node the IRNode with position info
	 */
	public void setPosition(IRNode node) {
		if (node.hasPosition()) {
			setPosition(node.getFromLineNumber(), node.getFromColumnNumber(), node.getToLineNumber(), node.getToColumnNumber());
		}
	}
	/**
	 * Updates the position information to begin at the beginning of {@code from} and end at the end of {@code to}.
	 *
	 * @param from start position
	 * @param to end position
	 */
	public void setPosition(IRNode from, IRNode to) {
		if (from.hasPosition() && to.hasPosition()) {
			setPosition(from.getFromLineNumber(), from.getFromColumnNumber(), to.getToLineNumber(), to.getToColumnNumber());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IRNode clone() {
		try {
			return (IRNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}
