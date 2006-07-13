package org.jacorb.test.bugs.bugjac174;

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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;


/**
 * <code>InvalidIORTest</code> tests that JacORB can decode and catch
 * Invalid IOR (these contain corrupt buffer lengths)
 *
 * @author Nick Cross
 * @version $Id$
 */
public class InvalidIORTest_bug174 extends TestCase
{
    /**
     * <code>ior1</code> is a invalid ior to decode.
     */
    private static final String ior1="IOR:000000000000001B49444C3A7274636F7262612F446174614F626A6563743A312E300000000000010000000000000076000102000000000E3133372E37382E36322E3138390000003EE400000000002700000000000000020000000073B8793A000005020708526F6F74504F412F6368696C64504F4100000000000200000000000000080000000054475802000000020000001600000000000000280000000A00000000000000013E80";
    /**
     * <code>ior2</code> is an invalid ior to decode.
     */
    private static final String ior2="IOR:000000000000001B49444C3A7274636F7262612F446174614F626A6563743A312E300000000000010000000000000076000102000000000E3133372E37382E36322E3138390000003EE400000000002700000000000000010000000073B8793A000005020708526F6F74504F412F6368696C64504F4100000000000200000000000000080000000054475802000000020000001600000000000000280000000A00000000000000013E80";

    /**
     * <code>orb</code> is used to obtain the root poa.
     */
    private org.omg.CORBA.ORB orb;

    protected void setUp() throws Exception
    {
        orb = ORB.init(new String[0], null);
    }

    protected void tearDown() throws Exception
    {
        orb.shutdown(true);
    }

    /**
     * <code>suite</code> lists the tests for Junit to run.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        return new TestSuite(InvalidIORTest_bug174.class);
    }


    /**
     * <code>testDecode2</code> tests that JacORB can decode an invalid
     * IOR without throwing OutOfMemory but rather BAD_PARAM.
     */
    public void testDecode1 ()
    {
        try
        {
            orb.string_to_object(ior1);
            fail();
        }
        catch (BAD_PARAM e)
        {
            // expected
        }
    }


    /**
     * <code>testDecode2</code> tests that JacORB can decode an invalid
     * IOR without throwing OutOfMemory but rather BAD_PARAM.
     */
    public void testDecode2 ()
    {
        try
        {
            orb.string_to_object(ior2);
            fail();
        }
        catch (BAD_PARAM e)
        {
            // expected
        }
    }
}
