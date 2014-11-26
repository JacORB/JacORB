package org.jacorb.test.harness;

import org.junit.experimental.categories.Category;


/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

/**
 * A ClientServerTestCase that is used to isolate those tests that require a
 * fixed port. These tests will not be run in parallel by Maven Surefire.
 *
 * @author rnc
 */
// Annoyingly it seems inherited categories don't aggregegate.
@Category({IMRExcludedClientServerCategory.class, ClientServerCategory.class, FixedPortCategory.class})
public abstract class FixedPortClientServerTestCase extends ClientServerTestCase
{
    /**
     * Locate an available port.
     *
     */
    protected static int getNextAvailablePort()
    {
        return TestUtils.getNextAvailablePort();
    }
}
