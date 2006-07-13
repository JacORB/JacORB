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

import junit.framework.TestCase;

import org.omg.CORBA.BAD_PARAM;

/**
 * <code>InvalidIORTest</code> tests that JacORB can decode a Invalid IOR - this
 * are a GIOP UDP protocol.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class InvalidIORTest extends TestCase
{
    /**
     * <code>orb</code> is used to obtain the root poa.
     */
    private org.omg.CORBA.ORB orb;

    /**
     * <code>ior1</code> is a valid ior to decode.
     */
    private static final String ior1="IOR:000000000000003049444C3A6F72672F6A61636F72622F696D722F496D706C656D656E746174696F6E5265706F7369746F72793A312E300000000002000000000000007C0001020000000020657863616C696275722E707269736D746563686E6F6C6F676965732E636F6D00BF070000000000127468655F496D522F496D52504F412F496D520000000000020000000000000008000000004A414300000000010000001C00000000000100010000000105010001000101090000000105010001000000010000002C0000000000000001000000010000001C00000000000100010000000105010001000101090000000105010001";
    /**
     * <code>ior2</code> is an invalid ior to decode.
     */
    private static final String ior2="IOR:0000000000000033---invalidated---F746966794368616E6E656C41646D696E2F4576656E744368616E6E656C3A312E3000000000000200000000000000C4000102000000000E3133392E32312E31362E3137300027EE0000006F4F70656E467573696F6E2E4E6F74696669636174696F6E536572766963652F4F70656E467573696F6E2E4E6F74696669636174696F6E536572766963652F3F7FCA61E61911D8A2DEDE0D3D5ECE43713F7FCA60E61911D8A2DEDE0D3D5ECE43DD805B00D97411D8BDE88382A8A0976C00000000020000000000000008000000004A414300000000010000001C0000000000010001000000010501000100010109000000010501000100000001000000500000000000000002000000010000001C00000000000100010000000105010001000101090000000105010001000000010000001C00000000000100010000000105010001000101090000000105010001";

    protected void setUp() throws Exception
    {
        orb = org.omg.CORBA.ORB.init(new String[0], null);
    }

    protected void tearDown() throws Exception
    {
        orb.shutdown(true);
    }

    /**
     * <code>testDecode1</code> tests that JacORB can decode a valid IOR.
     */
    public void testDecode1 ()
    {
        orb.string_to_object(ior1);
    }


    /**
     * <code>testDecode2</code> tests that JacORB can decode an invalid
     * IOR withour throwing OutOfMemory but rather BAD_PARAM.
     */
    public void testDecode2 () throws Exception
    {
        try
        {
            orb.string_to_object(ior2);
            fail();
        }
        catch (BAD_PARAM e)
        {
            // Correct exception - passed!
        }
    }
}
