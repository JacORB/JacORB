package org.jacorb.util;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.LoggerFactory;

/**
 * <code>ConsoleLoggerFactory</code> is a very simple example to demonstrate
 * overriding the default logger factory.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class ConsoleLoggerFactory implements LoggerFactory
{
    /**
     * <code>target</code> is the logger for this factory.
     */
    private Logger target;


    /**
     * <code>ConsoleLoggerFactory</code> creates a new Avalon console logger.
     */
    public ConsoleLoggerFactory()
    {
        target = new ConsoleLogger();
    }


    /**
     * <code>getLoggingBackendName</code> returns the name of the backend.
     *
     * @return the name of the actual logging mechanism, e.g., "logkit"
     */
    public String getLoggingBackendName()
    {
        return "console";
    }


    /**
     * <code>getNamedLogger</code> returns the logger for name. As this is
     * an example it simply returns the console target.
     *
     * @param name a <code>String</code> value
     * @return a console Logger for a given name
     */
    public Logger getNamedLogger(String name)
    {
        return target;
    }


    /**
     * <code>getNamedRootLogger</code> returns the logger for name. As this is
     * an example it simply returns the console target.
     *
     * @param name a <code>String</code> value
     * @return a console Logger for a given name
     */
    public Logger getNamedRootLogger(String name)
    {
        return target;
    }


    /**
     * <code>getNamedLogger</code> returns the logger for name. As this is
     * an example it simply returns the console target.
     *
     * @param name a <code>String</code> value
     * @param fileName a <code>String</code> value
     * @param maxFileSize a <code>long</code> value
     * @return a name Logger for a given  file name and max size
     * @exception java.io.IOException if an error occurs
     */
    public Logger getNamedLogger(String name, String fileName, long maxFileSize)
        throws java.io.IOException
    {
        return target;
    }


    /**
     * Set the file name and max file size for logging to a file
     *
     * @param fileName a <code>String</code> value
     * @param maxLogSize a <code>long</code> value
     * @exception java.io.IOException if an error occurs
     */
    public void setDefaultLogFile(String fileName, long maxLogSize)
        throws java.io.IOException
    {
    }

    public void configure(Configuration arg0) throws ConfigurationException
    {
        // nothing to configure
    }
}
