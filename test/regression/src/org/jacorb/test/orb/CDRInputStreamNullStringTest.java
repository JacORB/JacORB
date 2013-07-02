package org.jacorb.test.orb;
/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 2000-2012 Gerald Brose / The JacORB Team.
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
 *
 */
import junit.framework.TestSuite;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.ORBTestCase;

public class CDRInputStreamNullStringTest extends ORBTestCase
{

    public static TestSuite suite()
    {
        return new TestSuite( CDRInputStreamNullStringTest.class );
    }

    protected void patchORBProperties(String testName, java.util.Properties properties) throws Exception
    {
    	properties.put("jacorb.interop.null_string_encoding", "on");
    }

    public void testZeroSizedNullEncodedString() throws Exception
    {
        byte[] codedText = {0,0,0,0};
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        assertEquals( "read_string ", null, stream.read_string() );
    }

    public void testCorrectlyEncodedEmptyString() {
        byte[] codedText = {0,0,0,1,0};
        CDRInputStream stream = new CDRInputStream( orb, codedText );
        assertEquals( "read_string of size 1", "", stream.read_string() );
    }

    public void testWriteNullString() throws Exception
    {
        CDROutputStream cdr = new CDROutputStream (orb);
        cdr.write_string(null);
        cdr.write_boolean(true);
        cdr.write_string(null);
        cdr.write_string("TEST");

        CDRInputStream read = (CDRInputStream)cdr.create_input_stream();

        assertEquals (null, read.read_string());
        read.read_boolean();
        assertEquals (null, read.read_string());
        assertEquals ("TEST", read.read_string());
    }
}
