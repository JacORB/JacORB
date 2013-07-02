package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.Any;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TypeCode;

public class TypeCodeTest extends ORBTestCase
{
    class MyClass
    {
        String member1;
        String member2;
    }

    /**
     * Test that jacorb handles some self-constructed broken typecodes
     * well. The constructed typecode is in principal recursive, but not
     * flagged as such.
     */
    public void testBrokenRecursiveTypecode()
    {
        Any innerAny = orb.create_any();
        innerAny.insert_long(4711);

        StructMember[] members = {new StructMember("myAny",
                                                   innerAny.type(),
                                                   null)};

        TypeCode innerTc = orb.create_struct_tc(
            "IDL:Anonymous:1.0", // repository ID
            "Anonymous", // Struct name
            members);

        TypeCode outerTc = orb.create_struct_tc(
            "IDL:Anonymous:1.0", // repository ID
            "Anonymous", // Struct name
            new StructMember[]{new StructMember("foo", innerTc, null)});

        org.jacorb.orb.CDROutputStream out =
            new org.jacorb.orb.CDROutputStream(orb);
        out.write_TypeCode(outerTc);
        org.jacorb.orb.CDRInputStream in =
            new org.jacorb.orb.CDRInputStream(orb, out.getBufferCopy());

        out = new org.jacorb.orb.CDROutputStream(orb);

        //need to write out typecode, to check it's consistency completely
        out.write_TypeCode(in.read_TypeCode());
    }

    public void testEquals()
    {
        TypeCode tc = orb.create_string_tc(10);
        assertFalse(tc.equals("bla"));
    }

    public void testCreateDynamicTypeCode() throws Exception
    {
        TypeCode typeCode = org.jacorb.orb.TypeCode.create_tc(MyClass.class);

        assertEquals(2, typeCode.member_count());
        assertEquals(typeCode.member_type(0).type_modifier(), typeCode.member_type(1).type_modifier());
    }
}
