package org.jacorb.config;

import org.jacorb.orb.ORB;

/**
 * Can be subclassed to provide initialization of a backend logging system
 * based on parameters from the JacORB configuration.
 *
 * Note: This is an abstract class rather than an interface because it will
 * get a few general-purpose methods that subclasses will want to use.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public abstract class LoggingInitializer
{
    public static final String ATTR_LOG_VERBOSITY = "jacorb.log.default.verbosity";
    public static final String ATTR_LOG_FILE      = "jacorb.logfile";
    public static final String ATTR_LOG_APPEND    = "jacorb.logfile.append";
    
    public abstract void init (ORB orb,
                               Configuration configuration);            
}
