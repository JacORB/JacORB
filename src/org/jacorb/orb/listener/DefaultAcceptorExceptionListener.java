/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) The JacORB project, 1997-2006.
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

package org.jacorb.orb.listener;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.util.ObjectUtil;

/**
 * The JacORB default implementation of interface
 * <code>AcceptorExceptionListener</code>. It will shutdown the ORB on all
 * Errors and for SSLExceptions that are thrown on the first loop.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DefaultAcceptorExceptionListener
    implements AcceptorExceptionListener
{
    /**
     * <code>sslException</code> is a cached class name for ssl exceptions.
     */
    private Class sslException;

    /**
     * <code>logger</code> is the logger.
     */
    private final Logger logger;

    /**
     * Creates a new <code>DefaultAcceptorExceptionListener</code> instance.
     *
     */
    public DefaultAcceptorExceptionListener(Configuration configuration)
    {
        try
        {
            String exceptionClass = configuration.getAttribute("javax.net.ssl.SSLException");
            sslException = ObjectUtil.classForName(exceptionClass);
        }
        catch(ClassNotFoundException cnf) {} catch (ConfigurationException e)
        {
            sslException = null;
        }
        {
            sslException = null;
        }

        logger = configuration.getNamedLogger("jacorb.orb.iiop");
    }

    /**
     * Throwable <code>th</code> has been caught by the acceptor thread.
     *
     * @param ae an <code>AcceptorExceptionEvent</code> value
     */
    public void exceptionCaught(AcceptorExceptionEvent ae)
    {
        logger.fatalError
            ("DefaultAcceptorExceptionListener#exceptionCaught", ae.getException());

        if (logger.isDebugEnabled())
        {
            logger.debug("Caught acceptor event: " + ae);
        }

        if ((ae.getException() instanceof Error) ||
            (
                ! ((org.jacorb.orb.iiop.IIOPListener.Acceptor)ae.getSource()).getAcceptorSocketLoop()
                && (sslException != null && sslException.isInstance(ae.getException()))
            )
           )
        {
            ae.getORB().shutdown(true);
        }
    }
}
