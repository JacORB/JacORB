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

package org.jacorb.test.bugs.bugjac456;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Test;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * @author Alphonse Bendt
 */
public class BugJac456Test extends ORBTestCase
{
    @Test
    public void testAvailable() throws Exception
    {
        OutputStream out = orb.create_output_stream();
        out.write_long(1234);
        InputStream in = out.create_input_stream();

        assertEquals(4, in.available());

        in.read_octet();
        in.read_octet();
        in.read_octet();
        in.read_octet();
    }

    @Test
    public void testMarshalUsingJacorbApi1()
    {
        CDROutputStream out = new CDROutputStream();

        out.write_long(1234);

        byte[] data = out.getBufferCopy();
        verifyDataUsingJacorbApi(data);
        verifyDataUsingPortableApi(data);

        out.close();
    }

    @Test
    public void testMarshalUsingJacorbApi2()
    {
        CDROutputStream out = new CDROutputStream(orb);

        out.write_long(1234);

        byte[] data = out.getBufferCopy();
        verifyDataUsingJacorbApi(data);
        verifyDataUsingPortableApi(data);

        out.close();
    }

    @Test
    public void testMarshalUsingPortableApi() throws Exception
    {
        OutputStream out = orb.create_output_stream();
        out.write_long(1234);
        InputStream in = out.create_input_stream();
        byte[] data = new byte[5];
        in.read(data, 0, in.available());

        verifyDataUsingJacorbApi(data);
        verifyDataUsingPortableApi(data);
    }

    private void verifyDataUsingJacorbApi(byte[] data)
    {
        assertNotNull(data);

        CDRInputStream in = new CDRInputStream(orb, data);
        assertEquals(1234, in.read_long());

        CDRInputStream in2 = new CDRInputStream(data);
        assertEquals(1234, in2.read_long());

        in.close();
        in2.close();
    }

    private void verifyDataUsingPortableApi(byte[] data)
    {
        assertNotNull(data);

        OutputStream out = orb.create_output_stream();
        out.write_octet_array(data, 0, data.length);
        InputStream in = out.create_input_stream();
        assertEquals(1234, in.read_long());
    }
}
