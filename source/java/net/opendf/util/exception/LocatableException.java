
/*
BEGINCOPYRIGHT X
	
	Copyright (c) 2007, Xilinx Inc.
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
	- Neither the name of the copyright holder nor the names 
	  of its contributors may be used to endorse or promote 
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

package net.opendf.util.exception;

/**
 * The LocatableException is an exception wrapper that provides
 * relevent context information such as source file, etc.
 *
 * <p>Created: Tue Oct 02 16:00:57 2007
 *
 * @author imiller, last modified by $Author: imiller $
 * @version $Id:$
 */
public class LocatableException extends RuntimeException
{

    private String locate;

    /**
     * Creates a new LocatableException
     *
     * @param cause non-null Throwable cause of the exception
     * @param locate a non-null String identifying the location of the
     * exceptional event.  The location is relative to user experience
     * and not the location in the source code.
     */
    public LocatableException (Throwable cause, String locate)
    {
        super(cause);
        this.locate = locate;
    }

    public String getLocation ()
    {
        return this.locate;
    }

    /**
     * Lightweight class to distinguish between user locations and
     * internal tool/infrastructure locations.
     */
    public static class Internal extends LocatableException
    {
        public Internal (Throwable cause, String locate)
        {
            super(cause, locate);
        }
    }
    
}
