package org.jacorb.config;

import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * 
 * Implementation of org.slf4j.Logger that wraps the avalon-logkit
 * logging system. Configuration of LogKit is left to the user.
 * 
 * LogKit accepts only String messages.
 * Therefore, this implementation converts object messages into strings
 * by called their toString() method before logging them.
 *
 * @author 
 * @version $Id$
 */

public class LogKitLogger 
    extends MarkerIgnoringBase
{

    private static final long serialVersionUID = 668131851315507894L;
    
    private final static String name = "logkit";

    /** Logging goes to this LogKit logger */
    protected transient Logger logger = null;

    public LogKitLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * Return the underlying Logger we are using.
     */
    public Logger getLogger() 
    {
        if (logger == null) 
        {
            logger = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
        }
        return logger;
    }

    /**
     * Log message to LogKit logger with DEBUG priority.
     */
    public void trace(String message) 
    {
        debug(message);
    }


    /**
     * Log error to LogKit logger with DEBUG priority.
     */
    public void trace(String message, Throwable t) 
    {
        debug(message, t);
    }

    /**
     * Log message to LogKit logger with DEBUG priority.
     */
    public void debug(String message) 
    {
        if (message != null) 
        {
            getLogger().debug(String.valueOf(message));
        }
    }

    /**
     * Log error to LogKit logger with DEBUG priority.
     */
    public void debug(String message, Throwable t) 
    {
        if (message != null) 
        {
            getLogger().debug(String.valueOf(message), t);
        }
    }

    /**
     * Log message to LogKit logger with INFO priority.
     */
    public void info(String message) 
    {
        if (message != null) 
        {
            getLogger().info(String.valueOf(message));
        }
    }

    /**
     * Log error to LogKit logger with INFO priority.
     */
    public void info(String message, Throwable t) 
    {
        if (message != null) 
        {
            getLogger().info(String.valueOf(message), t);
        }
    }

    /**
     * Log message to LogKit logger with WARN priority.
     */
    public void warn(String message) 
    {
        if (message != null) 
        {
            getLogger().warn(String.valueOf(message));
        }
    }

    /**
     * Log error to LogKit logger with WARN priority.
     */
    public void warn(String message, Throwable t) 
    {
        if (message != null) 
        {
            getLogger().warn(String.valueOf(message), t);
        }
    }

    /**
     * Log message to LogKit logger with ERROR priority.
     */
    public void error(String message) 
    {
        if (message != null)
        {
            getLogger().error(String.valueOf(message));
        }
    }

    /**
     * Log error to LogKit logger with ERROR priority.
     */
    public void error(String message, Throwable t) 
    {
        if (message != null) 
        {
            getLogger().error(String.valueOf(message), t);
        }
    }

    /**
     * Check whether the LogKit logger will log messages of priority DEBUG.
     */
    public boolean isDebugEnabled() 
    {
        return getLogger().isDebugEnabled();
    }


    /**
     * Check whether the LogKit logger will log messages of priority ERROR.
     */
    public boolean isErrorEnabled() 
    {
        return getLogger().isErrorEnabled();
    }

    /**
     * Check whether the LogKit logger will log messages of priority INFO.
     */
    public boolean isInfoEnabled() 
    {
        return getLogger().isInfoEnabled();
    }

    /**
     * Check whether the LogKit logger will log messages of priority DEBUG.
     */
    public boolean isTraceEnabled() 
    {
        return getLogger().isDebugEnabled();
    }

    /**
     * Check whether the LogKit logger will log messages of priority WARN.
     */
    public boolean isWarnEnabled() 
    {
        return getLogger().isWarnEnabled();
    }

    // Following methods are org.slf4j.Logger specific 
    // just print the message
    public void debug(String message, Object arg1)
    {
        debug(message);
    }

    public void debug(String message, Object[] arg1)
    {
        debug(message);
    }

    public void debug(String message, Object arg1, Object arg2)
    {
        debug(message);
    }

    public void error(String message, Object arg1)
    {
        error(message);
    }

    public void error(String message, Object[] arg1)
    {
        error(message);
    }

    public void error(String message, Object arg1, Object arg2)
    {
        error(message);
    }

    public void info(String message, Object arg1)
    {
        info(message);
    }

    public void info(String message, Object[] arg1)
    {
        info(message);
    }

    public void info(String message, Object arg1, Object arg2)
    {
        info(message);
    }

    public void trace(String message, Object arg1)
    {
        trace(message);
    }

    public void trace(String message, Object[] arg1)
    {
        trace(message);
    }

    public void trace(String message, Object arg1, Object arg2)
    {
        trace(message);
    }

    public void warn(String message, Object arg1)
    {
        warn(message);
    }

    public void warn(String message, Object[] arg1)
    {
        warn(message);
    }

    public void warn(String message, Object arg1, Object arg2)
    {
        warn(message);
    }
}
