package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2006 Gerald Brose
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

import java.security.SecureRandom;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author Nick Cross
 * @version $Id$
 */
public class SSLRandom implements Configurable
{
    /**
     * <code>randomImpl</code> is a, possibly null, instantiation of the JSRandom
     * plugin interface.
     */
    private JSRandom randomImpl;

    /**
     * <code>logger</code> is the logger for the SSL factories.
     */
    protected Logger logger;

    /**
     * The <code>getSecureRandom</code> will return a SecureRandom object from the plugin.
     * If a class has not been configured this will return null which will mean the JSSE
     * will use its default instantiation.
     *
     * @return a <code>SecureRandom</code> value
     */
    protected SecureRandom getSecureRandom()
    {
        SecureRandom result = null;

        if (randomImpl != null)
        {
            result = randomImpl.getSecureRandom();
        }
        return result;
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        org.jacorb.config.Configuration config = (org.jacorb.config.Configuration) configuration;

        logger = config.getNamedLogger("jacorb.security.jsse");

        try
        {
            // Retrieve the class name from the configuration.
            randomImpl = (JSRandom) config.getAttributeAsObject("jacorb.security.randomClassPlugin");
        }
        catch (ConfigurationException e)
        {
            // ignore
        }
    }
}