/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.orb.iiop.IIOPListener;
import org.jacorb.util.ObjectUtil;
import org.slf4j.Logger;

/**
 * The JacORB default implementation of interface
 * <code>AcceptorExceptionListener</code>. It will shutdown the ORB on all
 * Errors and for SSLExceptions that are thrown on the first loop.
 *
 * @author Nick Cross
 */
public class DefaultAcceptorExceptionListener
    implements AcceptorExceptionListener, Configurable
{
    /**
     * <code>sslException</code> is a cached class name for ssl exceptions.
     */
    private Class sslException;

    /**
     * <code>logger</code> is the logger.
     */
    private Logger logger;

    /**
     * Creates a new <code>DefaultAcceptorExceptionListener</code> instance.
     *
     */
    public void configure(Configuration configuration)
    {
        sslException = null;
        String exceptionClass = configuration.getAttribute
        (
            "javax.net.ssl.SSLException", null
        );
        if (exceptionClass != null)
        {
            try
            {
                sslException = ObjectUtil.classForName(exceptionClass);
            }
            catch (ClassNotFoundException e)
            {
                // ignore
            }
        }
        logger = ((org.jacorb.config.Configuration)configuration).getLogger("jacorb.orb.iiop");
    }

    /**
     * Throwable <code>th</code> has been caught by the acceptor thread.
     *
     * @param e an <code>AcceptorExceptionEvent</code> value
     */
    public void exceptionCaught(AcceptorExceptionEvent e)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Caught acceptor event", e.getException());
        }

        if ((e.getException() instanceof Error) ||
            (
                ! ((IIOPListener.Acceptor)e.getSource()).getAcceptorSocketLoop()
                && (sslException != null && sslException.isInstance(e.getException()))
            )
           )
        {
            logger.error("fatal exception. will shutdown orb", e.getException());

            e.getORB().shutdown(true);
        }
    }
}
