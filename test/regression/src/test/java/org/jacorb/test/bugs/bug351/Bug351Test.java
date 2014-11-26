package org.jacorb.test.bugs.bug351;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for bug 351, marshaling of a complex valuetype.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 */
public class Bug351Test extends ClientServerTestCase
{
    private ValueServer server;

    @BeforeClass
    public static void beforeClassSetUp () throws Exception
    {
        setup = new ClientServerSetup (ValueServerImpl.class.getName());
    }

    @Before
    public void setUp()
    {
        server = ValueServerHelper.narrow(setup.getServerObject());
    }

    @Test
    public void testBug()
    {
        RetrievalResult result = server.search();
        assertTrue (result != null);
        float[] scores = result.getScores();
        assertEquals (1.2f, scores[0], 0.0f);
        assertEquals (3.4f, scores[1], 0.0f);
        assertEquals (5.6f, scores[2], 0.0f);
    }
}
