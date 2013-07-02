/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.orb.typecode;

import java.util.HashMap;
import java.util.Map;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.typecode.PrimitiveTypeCodeReader;
import org.jacorb.orb.typecode.TypeCodeReader;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.OutputStream;

/**
 * @author Alphonse Bendt
 */
public class PrimitiveTypeCodeReaderTest extends ORBTestCase
{
    private TypeCodeReader objectUnderTest = new PrimitiveTypeCodeReader();
    private Map repeatedTypeCodeMap = new HashMap();
    private Map recursiveTypeCodeMap = new HashMap();

    public void testPrimitiveTypeCodes()
    {
        int[] kinds = new int[] {
                TCKind._tk_null,
                TCKind._tk_void,
                TCKind._tk_short,
                TCKind._tk_long,
                TCKind._tk_ushort,
                TCKind._tk_ulong,
                TCKind._tk_float,
                TCKind._tk_double,
                TCKind._tk_boolean,
                TCKind._tk_char,
                TCKind._tk_octet,
                TCKind._tk_any,
                TCKind._tk_TypeCode,
                TCKind._tk_Principal,
                TCKind._tk_longlong,
                TCKind._tk_ulonglong,
                TCKind._tk_wchar,
        };

        for (int i = 0; i < kinds.length; i++)
        {
            runTest(kinds[i]);
        }
    }

    public void testWithNonPrimitiveTCKind()
    {
        CDRInputStream in = getInputStreamFromWithLong(-1);

        try
        {
            objectUnderTest.readTypeCode(in, recursiveTypeCodeMap, repeatedTypeCodeMap);
            fail();
        }
        catch(BAD_PARAM e)
        {
        }

        // NOTE: even though 14 and 29 are not considered primitive
        // they are handled like they were. courtesy to the sun orb
        // see the comment in org.jacorb.orb.TypeCode

        in = getInputStreamFromWithLong(15);

        try
        {
            objectUnderTest.readTypeCode(in, recursiveTypeCodeMap, repeatedTypeCodeMap);
            fail();
        }
        catch(BAD_PARAM e)
        {
        }
    }

    private CDRInputStream getInputStreamFromWithLong(final int i)
    {
        OutputStream out = orb.create_output_stream();
        out.write_long(i);
        CDRInputStream in = (CDRInputStream) out.create_input_stream();
        return in;
    }

    private void runTest(final int typeCodeKind)
    {
        TypeCode typeCode = orb.get_primitive_tc(TCKind.from_int(typeCodeKind));
        OutputStream out = orb.create_output_stream();
        out.write_TypeCode(typeCode);

        CDRInputStream in = (CDRInputStream) out.create_input_stream();
        TypeCode result = objectUnderTest.readTypeCode(in, recursiveTypeCodeMap, repeatedTypeCodeMap);

        assertTrue(typeCode.equal(result));
        assertTrue(recursiveTypeCodeMap.isEmpty());
        assertTrue(repeatedTypeCodeMap.isEmpty());
    }
}
