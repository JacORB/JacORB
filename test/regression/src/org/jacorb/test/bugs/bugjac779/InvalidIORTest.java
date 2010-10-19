package org.jacorb.test.bugs.bugjac779;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;


/**
 * <code>InvalidIORTest</code> tests that JacORB can decode a known IOR.
 *
 * @author Nick Cross
 */
public class InvalidIORTest extends ORBTestCase
{
    /**
     * <code>ior</code> is a valid ior to decode.
     */
    private static final String ior="IOR:000000000000003449444C3A436F7354797065644576656E744368616E6E656C41646D696E2F54797065644576656E744368616E6E656C3A312E30000000000100000000000000980101022A06000000643261703100C5C31B00000014010F00525354FA4BB34C7AD300000700000001000000080000002A040000000000000008000000012A2A2A004F41540100000018000000012A2A2A01000100010000000100010509010100000000000300000010000000012A2A2A05000000636E7331002AC5C30300000014000000012A2A2A0A000000646E7331616C69617300C5C3";


    /**
     * <code>suite</code> lists the tests for Junit to run.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        return new TestSuite(InvalidIORTest.class);
    }


    /**
     * <code>testDecode</code> tests that JacORB can decode a valid
     * IOR.
     */
    public void testDecodeValidIOR ()
    {
        orb.string_to_object(ior);
    }
}
