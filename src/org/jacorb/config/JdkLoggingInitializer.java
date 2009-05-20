package org.jacorb.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.jacorb.orb.ORB;
import org.jacorb.util.ObjectUtil;

/**
 * A LoggingInitializer for the JDK logging system.
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class JdkLoggingInitializer extends LoggingInitializer
{

    /**
     * Returns true if the currently used SLF4J backend is the
     * JDK logging implementation.  This is true if and only if
     * the SLF4J-to-JDK adapter can be found on the classpath.
     */
    private boolean usingJdkLogging()
    {
        try
        {
            Class c = ObjectUtil.classForName ("org.slf4j.impl.JDK14LoggerAdapter");
            return c != null;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * For a string that contains a number from 0 to 4, returns the
     * corresponding JDK log level.
     */
    private Level toJdkLogLevel (String level)
    {
        if (level == null || level.length() == 0)
        {
            return Level.INFO;
        }
        else
        {
            try
            {
                int logLevel = Integer.parseInt (level.trim());
                switch (logLevel)
                {
                case 0: return Level.OFF;
                case 1: return Level.SEVERE;
                case 2: return Level.WARNING;
                default:
                case 3: return Level.INFO;
                case 4: return Level.FINEST;
                }                
            }
            catch (NumberFormatException ex)
            {
                throw new RuntimeException (ex);
            }
        }
    }
    
    public void init (ORB orb, Configuration config)
    {
        if (!usingJdkLogging()) return;
        String level = config.getAttribute (ATTR_LOG_VERBOSITY, null);
        String file  = config.getAttribute (ATTR_LOG_FILE, null);
        if (   (level != null && level.length() > 0)
            || (file != null && file.length() > 0))
        {
            java.util.logging.Logger rootLogger =
                java.util.logging.Logger.getLogger ("jacorb");
            rootLogger.setUseParentHandlers (false);
            rootLogger.setLevel (toJdkLogLevel (level));
            Handler handler = new ConsoleHandler(); 
            if (file != null && file.length() > 0)
            {
                try
                {
                    handler = new FileHandler
                    (
                        file,
                        config.getAttributeAsBoolean (ATTR_LOG_APPEND, false)
                    );
                }
                catch (java.io.IOException ex)
                {
                    System.err.println ("could not write log file");
                }
            }
            handler.setFormatter (new JacORBLogFormatter());
            rootLogger.addHandler (handler);
        }
    }
    
}
