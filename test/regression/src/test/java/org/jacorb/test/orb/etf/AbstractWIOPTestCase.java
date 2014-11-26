/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2006-2014 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

package org.jacorb.test.orb.etf;

import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.IMRExcludedClientServerCategory;
import org.jacorb.test.orb.etf.wiop.WIOPFactories;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/**
 * Abstract superclass for the WIOP tests.
 * @author Alphonse Bendt
 */
@Category(IMRExcludedClientServerCategory.class)
public class AbstractWIOPTestCase extends ClientServerTestCase
{
    protected BasicServer server;

    @Before
    public void setUp() throws Exception
    {
        WIOPFactories.setTransportInUse(false);
        server = BasicServerHelper.narrow( setup.getServerObject() );
    }

    @After
    public void tearDown() throws Exception
    {
        WIOPFactories.setTransportInUse(false);
        server._release();
        server = null;
    }
}