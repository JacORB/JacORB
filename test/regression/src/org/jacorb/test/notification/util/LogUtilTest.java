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

package org.jacorb.test.notification.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.util.LogUtil;
import org.slf4j.Logger;

/**
 * @author Alphonse Bendt
 */
public class LogUtilTest extends TestCase
{
    /**
     * Constructor for LogUtilTest.
     * 
     * @param name
     */
    public LogUtilTest(String name)
    {
        super(name);
    }

    public void testGetLogger()
    {
        Logger _logger = LogUtil.getLogger("name");
        assertNotNull(_logger);
    }

    public static Test suite()
    {
        return new TestSuite(LogUtilTest.class);
    }
}