package org.jacorb.config;

import org.jacorb.config.JacORBLogFormatter.ClockFormat;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.ORBSingleton;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A LoggingInitializer for the JDK logging system.
 * @author Andre Spiegel {@literal <spiegel@gnu.org>}
 */
public class JdkLoggingInitializer extends LoggingInitializer
{
    /**
     * True if the currently used SLF4J backend is the
     * JDK logging implementation.  This is true if and only if
     * the SLF4J-to-JDK adapter can be found on the classpath.
     */
    private static final boolean ISJDKLOGGING;

    /**
     * Use to determine whether a Java Logging configuration file has been
     * supplied to override the JacORB configuration.
     */
    private static final boolean USEJACORBCONFIG;

    static
    {
        USEJACORBCONFIG = ( System.getProperty("java.util.logging.config.file") == null );

        Class<?> c = null;
        try
        {
            c = ObjectUtil.classForName ("org.slf4j.impl.JDK14LoggerAdapter");
        }
        catch (Exception ex)
        {
        }
        ISJDKLOGGING = (c != null);

         // This horror is to ensure that the Singleton logging is
         // always set up first.  Otherwise what happens is either the
         // Singleton logging goes to console, or the full ORB logging
         // uses the Singleton log file. This is mainly because JDK
         // logging does not allow separate Logger('jacorb')
         // instances. Which means they use each others logfile.
         ORBSingleton.init ();
    }


    /**
     * Cache of the root logger for this system.
     */
    private Logger rootLogger;


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
                    // http://www.slf4j.org/apidocs/org/slf4j/bridge/SLF4JBridgeHandler.html
                    case 0:
                        return Level.OFF;
                    case 1:
                        return Level.SEVERE;
                    case 2:
                        return Level.WARNING;
                    default:
                    case 3:
                        return Level.INFO;
                    case 4:
                        return Level.FINER;
                    case 5:
                        return Level.FINEST;
                }
            }
            catch (NumberFormatException ex)
            {
                throw new RuntimeException (ex);
            }
        }
    }

    @Override
    public void init (Configuration config) throws ConfigurationException
    {
        if (!ISJDKLOGGING || !USEJACORBCONFIG)
        {
            return;
        }

        String level = config.getAttribute (ATTR_LOG_VERBOSITY, "3");
        String file  = config.getAttribute (ATTR_LOG_FILE, null);
        boolean showThread = config.getAttributeAsBoolean (ATTR_LOG_THREAD_ID, false);
        boolean showSrcInfo = config.getAttributeAsBoolean (ATTR_LOG_SRC_INFO, false);
        String clockFormat = config.getAttribute(ATTR_LOG_CLOCK, ClockFormat.NONE.toString());

        rootLogger = Logger.getLogger(LoggingInitializer.ATTR_LOG_NAME);
        rootLogger.setUseParentHandlers (false);
        rootLogger.setLevel (toJdkLogLevel (level));

        // Ensure there is only one handler
        purgeHandlers (rootLogger);

        Handler handler;

        if (file != null && file.length() > 0)
        {
            if (new File (file).isDirectory())
            {
                // Logging to a directory. Append $implname.
                file = file.concat(File.separatorChar + "$implname");
            }

            try
            {
                handler = new FileHandler
                (
                        substituteImplname (file, config),
                        config.getAttributeAsInteger (ATTR_LOG_SIZE, 0),
                        config.getAttributeAsInteger (ATTR_LOG_ROTATE, 1),
                        config.getAttributeAsBoolean (ATTR_LOG_APPEND, false)
                );
            }
            catch (java.io.IOException ex)
            {
                System.err.println ("could not write log file");
                ex.printStackTrace();
                handler = new ConsoleHandler();
            }
            catch (ConfigurationException ex)
            {
                System.err.println ("could not write log file due to configuration exception");
                ex.printStackTrace();
                handler = new ConsoleHandler();
            }
        }
        else
        {
            handler = new ConsoleHandler();
        }

        handler.setLevel(toJdkLogLevel(level));
        handler.setFormatter (new JacORBLogFormatter(showThread, showSrcInfo, ClockFormat.getClockFormat(clockFormat)));
        rootLogger.addHandler (handler);
    }


    @Override
    public void shutdownLogging ()
    {
        if (ISJDKLOGGING)
        {
            // Clear lock files.
            Handler handlers[] = rootLogger.getHandlers();

            purgeHandlers (rootLogger);

            if (handlers.length > 0)
            {
                // So we don't loose any logs revert to console based logging.
                java.util.logging.Handler c = new java.util.logging.ConsoleHandler();
                c.setFormatter (handlers[0].getFormatter());
                c.setLevel     (handlers[0].getLevel());
                rootLogger.addHandler (c);
             }
        }
    }


    private void purgeHandlers (java.util.logging.Logger rootLogger)
    {
        // Ensure there is only one handler
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers != null && handlers.length > 0)
        {
            for (int i = 0; i < handlers.length; i++)
            {
                handlers[i].close();
                rootLogger.removeHandler(handlers[i]);
            }
        }
    }
}
