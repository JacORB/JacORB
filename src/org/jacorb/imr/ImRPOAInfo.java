/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.imr;

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.imr.AdminPackage.*;

import org.jacorb.util.*;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.*;

/**
 * This class stores information about a POA. It also provides methods 
 * for reactivation, conversion, and for waiting for reactivation.
 *
 * @author Nicolas Noffke
 * 
 * @version $Id$
 */

public class ImRPOAInfo 
    implements java.io.Serializable
{
    protected int port;
    protected ImRServerInfo server;
    protected String host;
    protected String name;
    protected boolean active;
    protected long timeout; // 2 min.

    private org.jacorb.config.Configuration configuration;
    private Logger logger;

    /**
     * The constructor of this class.
     *
     * @param name the POAs name.
     * @param host the POAs host.
     * @param port the port the POA listens on.
     * @param server the server the POA is associated with.
     * @exception IllegalPOAName thrown when <code>name</code> is 
     * <code>null</code> or of length zero.
     */

    public ImRPOAInfo(String name, String host, int port, ImRServerInfo server) 
	throws IllegalPOAName 
    {
	if (name == null || name.length() == 0)
	    throw new IllegalPOAName(name);

	this.name = name;
	this.host = host;
	this.port = port;
	this.server = server;
	this.active = true;
    }

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.imr");
        // default timeout is 2 mins.
        timeout = configuration.getAttributeAsInteger( "jacorb.imr.timeout",120000);
    }


    /**
     * "Converts" this Object to an instance of the POAInfo class.
     * 
     * @return a POAInfo object.
     */  

    public POAInfo toPOAInfo()
    {
	return new POAInfo(name, host, port,server.name, active); 
    }

    /**
     * Reactivates this POA, i.e. sets it to active and unblocks any 
     * waiting threads.
     *
     * @param host the POAs new host.
     * @param port the POAs new port.
     */

    public synchronized void reactivate(String host, int port)
    {
	this.host = host;
	this.port = port;
	active = true;	
	server.active = true;
	server.restarting = false;
	notifyAll();
    }

    /**
     * This method blocks until the POA is reactivated, or the
     * timeout is exceeded.
     * 
     * @return false, if the timeout has been exceeded, true otherwise.
     */

    public synchronized boolean awaitActivation()
    {
	while(!active)
        {
	    try
            {
		long _sleep_begin = System.currentTimeMillis();
		wait(timeout);
		if (!active && 
                    (System.currentTimeMillis() - _sleep_begin) > timeout)
		{
                    if (logger.isDebugEnabled())
                        logger.debug("awaitActivation, time_out");
		    return false;
		}
	    }
            catch (java.lang.Exception e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("awaitActivation: " + e.getMessage());
	    }
	}
        if (logger.isDebugEnabled())
            logger.debug("awaitActivation, returns true");

	return true;
    }
} // ImRPOAInfo

