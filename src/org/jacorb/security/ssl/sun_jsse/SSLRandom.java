package org.jacorb.security.ssl.sun_jsse;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.slf4j.Logger;

/**
 * @author Nick Cross
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

    public void configure(Configuration config) throws ConfigurationException
    {
        logger = config.getLogger("jacorb.security.jsse");

        try
        {
            // Retrieve the class name from the configuration.
            randomImpl = (JSRandom) config.getAttributeAsObject("jacorb.security.randomClassPlugin");

            if (logger.isDebugEnabled())
            {
                logger.debug("Using JSRandom implemented by " + randomImpl);
            }
        }
        catch (ConfigurationException e)
        {
            // ignore
        }
    }
}