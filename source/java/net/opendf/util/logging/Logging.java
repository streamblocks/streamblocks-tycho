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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The Logging class is a factory which provides static methods for
 * accessing {@link Logger} instances for various user/developer
 * output streams.  Static methods allow easy access to these loggers
 * from anywhere in the code base.
 *
 * <p>Created: Wed Jan 03 10:37:04 2007
 *
 * @author imiller, last modified by $Author: imiller $
 * @version $Id: Logging.java 40 2007-01-10 21:17:38Z imiller $
 */
public class Logging
{

    // Debug logger.  Used by developers for insight into the
    // workings/errors in the tool
    private static Logger DBG = null;

    // User logger.  Used for user interactions
    private static Logger USER = null;

    private static Logger SIMSTATE = null;

    // user-generated simulation output
    private static Logger SIMOUT = null;

    /** A Logger {@link Level} which forces output to happen
     * regardless of the specified level. */
    public static final Level FORCE = new XCALLoggingLevel("FORCE", Level.OFF.intValue());

    /**
     * Returns a non-null Logger instance that is used for output of
     * tool debugging messages.
     *
     * @return a non-null {@link Logger}
     */
    public static Logger dbg ()
    {
        if (DBG == null)
        {
            init();
        }
        return DBG;
    }
    
    /**
     * Returns a non-null Logger instance that is used for output of
     * messages intended for consumption by the user.
     *
     * @return a non-null {@link Logger}
     */
    public static Logger user ()
    {
        if (USER == null)
        {
            init();
        }
        return USER;
    }

    /**
     * Returns a non-null Logger instance that is used for output of
     * user-generated messages from simulation.
     *
     * @return a non-null {@link Logger}
     */
    public static Logger simout ()
    {
        if (SIMOUT == null)
        {
            init();
        }
        return SIMOUT;
    }

    public static void setUserLevel (Level level)
    {
        if (USER == null)
            init();
        USER.setLevel(level);
    }
    
    public static void setDbgLevel (Level level)
    {
        if (DBG == null)
            init();
        DBG.setLevel(level);
    }

    public static void setSimoutLevel (Level level)
    {
        if (SIMOUT == null)
            init();
        SIMOUT.setLevel(level);
    }

    /**
     * Returns a non-null Logger instance that is used for output of
     * simulation status messages.
     *
     * @return a non-null {@link Logger}
     */
    public static Logger simState ()
    {
        if (SIMSTATE == null)
        {
            init();
        }
        return SIMSTATE;
    }
    
    /**
     * Initialization is deferred until the first call to access a
     * Logger so that we avoid a race condition with the reading of
     * the logging properties.
     */
    private static void init ()
    {
        DBG = Logger.getLogger("xcal.debug");
        USER = Logger.getLogger("xcal.user");
        SIMSTATE = Logger.getLogger("xcal.simstate");
        SIMOUT = Logger.getLogger("xcal.simout");
        
        // Check to see if we have explicit setting of the default
        // levels.  If not, then set some appropriate levels now
//        if (System.getProperty("java.util.logging.config.file") == null &&
//            System.getProperty("java.util.logging.config.class") == null)
//        {
//        }
        // Always set default levels.  If the user has set them in a config file they will be non null already
        if (DBG.getLevel() == null)      DBG.setLevel(Level.SEVERE);
        if (USER.getLevel() == null)     USER.setLevel(Level.INFO);
        if (SIMSTATE.getLevel() == null) SIMSTATE.setLevel(Level.INFO);
        if (SIMOUT.getLevel() == null)   SIMOUT.setLevel(Level.INFO);
        
        // The parent Loggers are the root which, by default, have the
        // ConsoleHandler defined which sends all output to std err.
        USER.setUseParentHandlers(false);
        addDefaultHandler(USER);

        DBG.setUseParentHandlers(false);
        addDefaultHandler(DBG);

        SIMSTATE.setUseParentHandlers(false);
        addDefaultHandler(SIMSTATE);

        SIMOUT.setUseParentHandlers(false);
        addDefaultHandler(SIMOUT);
    }
    
    /**
     * Removes the default handler if the logger is known to this class and 
     * the handler is registered, otherwise it fails silently
     * @param logger
     */
    public static void removeDefaultHandler (Logger logger)
    {
        if (logger == user()) user().removeHandler(DEFAULT_USER_HANDLER);
        if (logger == simout()) simout().removeHandler(DEFAULT_SIM_HANDLER);
        if (logger == dbg()) dbg().removeHandler(DEFAULT_DBG_HANDLER);
        if (logger == simState()) simState().removeHandler(DEFAULT_SIMSTATE_HANDLER);
    }
    /**
     * Adds the default handler to the logger if the logger is known.  It is safe to call 
     * this method multiple times as it will not doubly add the handler. 
     * @param logger
     */
    public static void addDefaultHandler (Logger logger)
    {
        // Remove it first to avoid duplication
        removeDefaultHandler(logger);
        if (logger == user()) user().addHandler(DEFAULT_USER_HANDLER);
        if (logger == simout()) simout().addHandler(DEFAULT_SIM_HANDLER);
        if (logger == dbg()) dbg().addHandler(DEFAULT_DBG_HANDLER);
        if (logger == simState()) simState().addHandler(DEFAULT_SIMSTATE_HANDLER);
    }
    
    private static final Handler DEFAULT_USER_HANDLER = new FlushedStreamHandler(System.out, new BasicLogFormatter()); 
    private static final Handler DEFAULT_DBG_HANDLER = new FlushedStreamHandler(System.out, new DebugLogFormatter(true, "dbg")); 
    private static final Handler DEFAULT_SIMSTATE_HANDLER = new FlushedStreamHandler(System.out, new DebugLogFormatter(true, "simstate")); 
    private static final Handler DEFAULT_SIM_HANDLER = new FlushedStreamHandler(System.out,  new DebugLogFormatter(true, "simout"));

    /**
     * Sub-class allowing us to specify the name and level
     */
    private static class XCALLoggingLevel extends Level
    {
        private XCALLoggingLevel (String name, int level)
        {
            super(name, level);
        }
    }
}
