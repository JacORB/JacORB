package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.List;

import junit.framework.TestCase;

import org.jacorb.orb.ParsedIOR;
import org.omg.CORBA.ORB;

/**
 * <code>DIOPIORTest</code> tests that JacORB can decode a DIOP IOR - this
 * are a GIOP UDP protocol.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DIOPIORTest extends TestCase
{
    private final org.omg.CORBA.ORB orb = ORB.init(new String[0], null);

    /**
     * <code>testIOR</code> is a test DIOP ior to decode.
     */
    private static final String ior = "IOR:0064d0820000002a49444c3a6e6f6465624361744361742f436174426173655265717565737448616e646c65723a312e3000531d0000000154414f0400000025000100540000000a3132372e302e302e310004010000000d654f524208000041c630303030";

    /**
     * <code>testDecode1</code> tests that JacORB can decode a DIOP IOR. To do
     * this we create a ParsedIOR with the known IOR and test that the number
     * of profile bodies is greater than zero.
     */
    public void testDecode1 ()
    {
        final ParsedIOR pior = new ParsedIOR((org.jacorb.orb.ORB) orb, ior);

        List bodies = pior.getProfiles();

        assertNotNull("did not get bodies", bodies);
        assertTrue("did not get bodies", bodies.size() > 0);
    }

    protected void tearDown() throws Exception
    {
        orb.shutdown(true);
    }
}
