package org.jacorb.config;

import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;

public class ConsoleLogger 
    extends MarkerIgnoringBase
{
    private static final long serialVersionUID = -4918847563921516018L;

    /** Typecode for debugging messages. */
    public static final int LEVEL_DEBUG = 0;
    
    /** Typecode for informational messages. */
    public static final int LEVEL_INFO = 1;
    
    /** Typecode for warning messages. */
    public static final int LEVEL_WARN = 2;

    /** Typecode for error messages. */
    public static final int LEVEL_ERROR = 3;

    /** Typecode for fatal error messages. */
    public static final int LEVEL_FATAL = 4;

    /** Typecode for disabled log levels. */
    public static final int LEVEL_DISABLED = 5;

    private final int logLevel;

    /**
     * Creates a new ConsoleLogger with the priority set to DEBUG.
     */
    public ConsoleLogger()
    {
        this( LEVEL_DEBUG );
    }

    /**
     * Creates a new ConsoleLogger.
     * @param logLevel log level typecode
     */
    public ConsoleLogger( final int logLevel )
    {
        this.logLevel = logLevel;
    }

    /**
     * Logs a debugging message.
     *
     * @param message a <code>String</code> value
     */
    public void debug(String message)
    {
        debug(message, (Throwable) null);
    }
 
    /**
     * Logs a debugging message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void debug(String message, Throwable throwable)
    {
        if (logLevel <= LEVEL_DEBUG)
        {
            System.out.print("[DEBUG]");
            System.out.println(message);
 
            if (throwable != null)
            {
                throwable.printStackTrace(System.out);
            }
        }
    }

    /**
     * Returns <code>true</code> if debug-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if debug-level logging
     */
    public boolean isDebugEnabled()
    {
        return logLevel <= LEVEL_DEBUG;
    }
 
    /**
     * Logs an informational message.
     *
     * @param message a <code>String</code> value
     */
    public void info(String message)
    {
        info(message, (Throwable) null);
    }
 
    /**
     * Logs an informational message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void info(String message, Throwable throwable)
    {
        if (logLevel <= LEVEL_INFO)
        {
            System.out.print("[INFO] ");
            System.out.println(message);

            if (throwable != null)
            {
                throwable.printStackTrace(System.out);
            }
        }
    }

    /**
     * Returns <code>true</code> if info-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if info-level logging is enabled
     */
    public boolean isInfoEnabled()
    {
        return logLevel <= LEVEL_INFO;
    }

    /**
     * Logs a warning message.
     *
     * @param message a <code>String</code> value
     */
    public void warn(String message)
    {
        warn( message, (Throwable) null );
    }

    /**
     * Logs a warning message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void warn(String message, Throwable throwable)
    {
        if (logLevel <= LEVEL_WARN)
        {
            System.out.print("[WARNING] ");
            System.out.println(message);

            if (throwable != null)
            {
                throwable.printStackTrace(System.out);
            }
        }
    }

    /**
     * Returns <code>true</code> if warn-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if warn-level logging is enabled
     */
    public boolean isWarnEnabled()
    {
        return logLevel <= LEVEL_WARN;
    }

    /**
     * Logs an error message.
     *
     * @param message a <code>String</code> value
     */
    public void error(String message)
    {
        error(message, (Throwable) null);
    }

    /**
     * Logs an error message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void error(String message, Throwable throwable)
    {
        if (logLevel <= LEVEL_ERROR)
        {
            System.out.print("[ERROR] ");
            System.out.println(message);

            if(throwable != null)
            {
                throwable.printStackTrace(System.out);
            }
        }
    }

    /**
     * Returns <code>true</code> if error-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if error-level logging is enabled
     */
    public boolean isErrorEnabled()
    {
        return logLevel <= LEVEL_ERROR;
    }

    /**
     * Logs a fatal error message.
     *
     * @param message a <code>String</code> value
     */
    public void fatalError(String message)
    {
        fatalError(message, null);
    }
    
    /**
     * Logs a fatal error message and an exception.
     *
     * @param message a <code>String</code> value
     * @param throwable a <code>Throwable</code> value
     */
    public void fatalError(String message, Throwable throwable)
    {
        if (logLevel <= LEVEL_FATAL)
        {
            System.out.print("[FATAL ERROR] ");
            System.out.println(message);
 
            if (throwable != null)
            {
                throwable.printStackTrace(System.out);
            }
        }
    }
 
    /**
     * Returns <code>true</code> if fatal-level logging is enabled, false otherwise.
     *
     * @return <code>true</code> if fatal-level logging is enabled
     */
    public boolean isFatalErrorEnabled()
    {
        return logLevel <= LEVEL_FATAL;
    }

    /**
     * Just returns this logger (<code>ConsoleLogger</code> is not hierarchical).
     *
     * @param name ignored
     * @return this logger
     */
    public Logger getChildLogger(String name)
    {
        return this;
    }

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

    public boolean isTraceEnabled()
    {
        return isDebugEnabled();
    }

    public void trace(String message)
    {
        debug(message);
    }

    public void trace(String message, Object arg1)
    {
        debug(message);
    }

    public void trace(String message, Object[] arg1)
    {
        debug(message);
    }

    public void trace(String message, Throwable t)
    {
        debug(message, t);
    }

    public void trace(String message, Object arg1, Object arg2)
    {
        debug(message);
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
