/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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

package org.jacorb.orb.factory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.TIMEOUT;
import org.slf4j.Logger;

/**
 * abstract base class for JacORB's default SocketFactory implementations.
 *
 * @author Alphonse Bendt
 */

public abstract class AbstractSocketFactory implements SocketFactory, Configurable
{
    protected Logger logger;

    public void configure(Configuration configuration) throws ConfigurationException
    {
        org.jacorb.config.Configuration config = (org.jacorb.config.Configuration) configuration;

        logger = config.getLogger("jacorb.orb.socketfactory");
    }

    /**
     * to ensure we throw the correct exception in case a timeout occurs we provide
     * a final implementation of this method here, delegate to subclass-specific implementations
     * and handle the correct conversion of the exception in one place.
     */
    public final Socket createSocket(String host, int port, int timeout) throws UnknownHostException, IOException
    {
    	try
    	{
    		return doCreateSocket(host, port, timeout);
    	}
    	catch(SocketTimeoutException e)
    	{
    		throw new TIMEOUT(e.toString());
    	}
    }

    protected abstract Socket doCreateSocket(String host, int port, int timeout) throws IOException, UnknownHostException;
}
