package org.jacorb.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;

import org.apache.log.*;
import org.apache.log.format.*;
import org.apache.log.output.io.*;
import org.apache.log.output.io.rotate.*;

import java.util.*;
import java.io.*;

/**
 * JacORB logger factory that creates named Avalon loggers with logkit
 * as the actual mechanism
 *
 * @author Gerald Brose 
 * @version $Id$
 * @since JacORB 2.0 beta 3
 */

class LogKitLoggerFactory 
    implements LoggerFactory
{
    private final static String name = "logkit";
    private final static PatternFormatter logFormatter =
        new PatternFormatter("[%.20{category}] %.7{priority} : %{message}\\n%{throwable}");

    /** default priority for loggers created with this factory */
    private int defaultPriority = 0;

    /** cache of created loggers */
    private final Hashtable namedLoggers = new Hashtable();

    /**  append to a log file or overwrite ?*/
    private boolean append = false;

    private Writer consoleWriter = null; 


    public LogKitLoggerFactory()
    {
        consoleWriter = new OutputStreamWriter(System.err);
    }

    public void setDefaultPriority(int priority)
    {
        defaultPriority = priority;
    }

    /**
     * @return the name of the actual logging mechanism, here "logkit"
     */
    public final String getLoggingBackendName()
    {
        return name;
    }

    /**
     * @return a Logger for a given name and which inherits its log
     * targets from its ancestors in the logging hierarchy
     */
    public Logger getNamedLogger(String name)
    {
        return getNamedLogger(name, null);
    }

    /**
     * @return a Logger for a given name and which logs to the console (System.err)
     */
    public Logger getNamedRootLogger(String name)
    {
        LogTarget target = new WriterTarget(consoleWriter, logFormatter);
        return getNamedLogger(name, target);
    }


    /**
     * @return a Logger for a given name with a given priority and
     * which logs to the a file log target until the log has size
     * maxLogSize, at which time the log will be rotated. A maxLogSize
     * of 0 means unlimited.  
     */
    public Logger getNamedLogger(String name, String logFileName, long maxLogSize)
        throws IOException
    { 
        if (name == null)
            throw new IllegalArgumentException("Log file name must not be null!");

        FileOutputStream logStream =
            new FileOutputStream(logFileName, append);

        LogTarget target = null;
        if (maxLogSize == 0 )
        {
            // no log file rotation
            Writer logWriter = new OutputStreamWriter(logStream);
            target = new WriterTarget(logWriter, logFormatter);
        }
        else
        {
            
            // log file rotation
            target =
                new RotatingFileTarget(append,
                                       logFormatter,
                                       new RotateStrategyBySize(maxLogSize * 1000),
                                       new RevolvingFileStrategy(new File(logFileName), 10000));
        }
        return getNamedLogger(name, target);
    }


    /**
     * @return a Logger for a given name with a given priority and
     * a given log target. Will log to console if target is null
     */
    public Logger getNamedLogger(String name, LogTarget target)
    {
        Object o = namedLoggers.get(name);

        if( o != null )
            return (Logger)o;
        
        org.apache.log.Logger logger = 
            Hierarchy.getDefaultHierarchy().getLoggerFor(name);

        String priorityString = Environment.getProperty( name + ".log.verbosity");

        int priority = defaultPriority;

        if (priorityString != null)
            priority = Integer.parseInt(priorityString);
            
        switch (priority)
        {
        case 4 :
            logger.setPriority(Priority.DEBUG);
            break;
        case 3 :
            logger.setPriority(Priority.INFO);
            break;
        case 2 :
            logger.setPriority(Priority.WARN);
            break;
        case 1 :
            logger.setPriority(Priority.ERROR);
            break;
        case 0 :
        default :
            logger.setPriority(Priority.FATAL_ERROR);
        }

        if (target != null )
        { 
            logger.setLogTargets( new LogTarget[] { target } );
        }

        Logger result = new LogKitLogger(logger);

        namedLoggers.put(name, result);

        return result;
    }





}
