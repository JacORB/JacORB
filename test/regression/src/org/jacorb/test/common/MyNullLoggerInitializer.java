/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.common;

import java.io.IOException;
import java.util.logging.Handler;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.config.LoggingInitializer;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class MyNullLoggerInitializer extends LoggingInitializer
{
    private final Logger nullLogger = new MyNullLogger();

    public void init (Configuration config)
    {
        java.util.logging.Logger rootLogger =
            java.util.logging.Logger.getLogger ("jacorb");

        // remove all handlers
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
