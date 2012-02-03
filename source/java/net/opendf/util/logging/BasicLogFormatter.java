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

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * BasicLogFormatter is a {@link java.util.logging.Formatter} which
 * formats messages according to the following format:<br>
 * [prefix] &lt;time&gt; Level: message
 *
 * <ul>
 * <li>The prefix is included only if defined
 * <li>The time is included based on a flag passed to this class when constructed
 * <li>The Level is included only for messages with severeity greater
 * than or equal to {@link Level#WARNING}
 * </ul>
 *
 * <p>Created: Wed Jan 03 11:49:11 2007
 *
 * @author imiller, last modified by $Author: imiller $
 * @version $Id: BasicLogFormatter.java 37 2007-01-03 22:00:11Z imiller $
 */
public class BasicLogFormatter extends java.util.logging.Formatter
{
    /** Date/format objects for use when tagging date to message */
    private Date date = new Date();
    private DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

    /** If set to true then the messages will include the time */
    private boolean doDate = false;
    /** Prefix may be null.  If non-null it will be prepended to the
     * message */
    private String prefix = null;

    /**
     * Constructs a formatter with the specified prefix and time-stamping.
     */
    public BasicLogFormatter (boolean tagWithDate, String prefix)
    {
        this.doDate = tagWithDate;
        this.prefix = prefix;
    }

    /**
     * Constructs a formatter with no prefix and no time stamping.
     */
    public BasicLogFormatter ()
    {
        this(false, null);
    }

    private long prevTime = -1;
    
    public String format (LogRecord record)
    {
        StringBuffer stringBuffer = new StringBuffer();
        if (this.prefix != null)
        {
            stringBuffer.append("[" + this.prefix + "]");
        }
        
        if (this.doDate)
        {
            date.setTime(record.getMillis());
            stringBuffer.append("<"+dateFormat.format(date)+ "> ");
            /*
            long delta = record.getMillis();
            if (prevTime > 0)
                delta = record.getMillis() - prevTime;
            prevTime = record.getMillis();
            stringBuffer.append("<"+delta+ "> ");
            */
        }

        if (record.getLevel() == Logging.FORCE)
            ; // add no severity to forced messages
        else if (record.getLevel().intValue() < Level.WARNING.intValue())
            ; // add no severity to levels below WARNING
        else 
            stringBuffer.append(record.getLevel().getName()+ ": ");
        
        stringBuffer.append(formatMessage(record));
        stringBuffer.append("\n");

        return stringBuffer.toString();
    }
    
}
