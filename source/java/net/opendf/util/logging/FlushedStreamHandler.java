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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * FlushedStreamHandler is a {@link Handler} which ensures that the
 * stream is flushed at every message processing.  This ensures that
 * if the stream is System.out or System.err the messages will appear
 * in correct order relative to other messages sent to those streams. 
 *
 * <p>Created: Wed Jan 03 12:27:46 2007
 *
 * @author imiller, last modified by $Author: jornj $
 * @version $Id: FlushedStreamHandler.java 55 2007-01-22 19:07:29Z jornj $
 */
public class FlushedStreamHandler extends Handler
{
    private static final String _RCS_ = "$Rev: 55 $";

    /** The writer used to access the stream */
    private PrintWriter writer;
    /** The particular output stream. */
    private OutputStream out;
    
    /**
     * Create a <tt>FlushedStreamHandler</tt> with a given <tt>Formatter</tt>
     * and output stream.
     * <p>
     * @param output 	the target output stream
     * @param formatter   Formatter to be used to format output
     */
    public FlushedStreamHandler(OutputStream out, Formatter formatter)
    {
        assert out != null : "Output stream must be non-null";
        this.out=out;
        setFormatter(formatter);
        writer=new PrintWriter(out,true);
    }

    /**
     * Flush any buffered messages.
     */
    public synchronized void flush()
    {
        try
        {
            writer.flush();
            out.flush();
        }
        catch (Exception ex) {;}
    }

    public synchronized void close() throws SecurityException
    {
        flush();
        writer.close();
    }

    public synchronized void publish(LogRecord record)
    {
        if (!isLoggable(record))
        {
            return;
        }

        try
        {
            String msg = getFormatter().format(record);
            writer.print(msg);
            writer.flush();
        }
        catch (Exception ex)
        {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }
}
