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

package org.jacorb.test.bugs.bugjac288;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.portable.InputStream;

/**
 * @author Alphonse Bendt
 */
public class BugJac288Test extends ORBTestCase
{
    @Test
    public void testDeMarshalEmptyStringReusesSameString()
    {
        assertSame(marshal(""), marshal(""));
    }

    private String marshal(String input)
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();
        out.write_string(input);

        byte[] buffer = out.getBufferCopy();

        CDRInputStream in = new CDRInputStream(buffer);
        String result = in.read_string();
        in.close();
        return result;
    }

    @Test
    public void testStringMayNotBeSizeZero()
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();
        out.write_long(0);

        InputStream in = out.create_input_stream();

        try
        {
            in.read_string();
            fail();
        }
        catch(MARSHAL e)
        {
        }
    }

    @Test
    public void testEmptyStringMustBeTerminated()
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();
        out.write_long(1);

        InputStream in = out.create_input_stream();

        try
        {
            in.read_string();
            fail();
        }
        catch(MARSHAL e)
        {
        }
    }

    @Test
    public void testEmptyStringMustBeTerminatedWithZeroOctet()
    {
        CDROutputStream out = (CDROutputStream) orb.create_output_stream();
        out.write_long(1);
        out.write_octet((byte) 1);
        InputStream in = out.create_input_stream();

        try
        {
            in.read_string();
            fail();
        }
        catch(MARSHAL e)
        {
        }
    }
}
