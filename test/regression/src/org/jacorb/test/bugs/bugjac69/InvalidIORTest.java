package org.jacorb.test.bugs.bugjac69;

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

import junit.framework.TestCase;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;


/**
 * <code>InvalidIORTest</code> tests that JacORB can decode a Invalid IOR - this
 * are a GIOP UDP protocol.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class InvalidIORTest extends TestCase
{
    private static org.omg.CORBA.ORB orb = ORB.init(new String[0], null);

    /**
     * <code>ior1</code> is an invalid ior (from the broken eorb before it was fixed)
     * to decode.
     */
    private static final String INVALID_IOR="IOR:010000001800000049444c3a4772656574696e67536572766963653a312e30000100000000000000250000000101000008000000302e312e322e3300f90a00000d000000654f524208b0a047560000000";


    /**
     * <code>testDecode1</code> tests that JacORB can decode a valid IOR.
     */
    public void testDecode1 ()
    {
        try
        {
            orb.string_to_object(INVALID_IOR);
            fail( "No Exception thrown");
        }
        catch (BAD_PARAM e)
        {
            // expected
        }
    }
}
