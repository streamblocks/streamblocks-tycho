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


package net.opendf.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * DebugLogFormatter is an extension of {@link BasicLogFormatter} which
 * adds handling for thrown exceptions.
 *
 * <p>Created: Tue Jan 08 15:53:04 2007
 *
 * @author imiller, last modified by $Author: imiller $
 * @version $Id: DebugLogFormatter.java 40 2007-01-10 21:17:38Z imiller $
 */
public class DebugLogFormatter extends BasicLogFormatter
{
    /**
     * Constructs a formatter with the specified prefix and time-stamping.
     */
    public DebugLogFormatter (boolean tagWithDate, String prefix)
    {
        super(tagWithDate, prefix);
    }

    public String format (LogRecord record)
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(super.format(record));

        final Throwable thrown = record.getThrown();
        if (thrown != null)
        {
            stringBuffer.append("\t" + thrown.getClass().toString() + ": " + thrown.getMessage());
            stringBuffer.append("\n");

            // Only print out the stack trace if the level is more
            // verbose than INFO
            if (record.getLevel().intValue() <= Level.INFO.intValue())
            {
                final StackTraceElement trace[] = thrown.getStackTrace();

                int limit = trace.length > 2 ? 2:trace.length;
                if (record.getLevel().intValue() <= Level.FINER.intValue())
                    limit = trace.length;
                for (int i=0; i < limit; i++)
                {
                    stringBuffer.append("\t" + trace[i].toString() + "\n");
                }
            }
        }

        return stringBuffer.toString();
    }
}
