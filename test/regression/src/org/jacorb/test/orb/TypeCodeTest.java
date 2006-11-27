package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

import junit.framework.*;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.*;

public class TypeCodeTest extends ORBTestCase
{
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
            new org.jacorb.orb.CDRInputStream(this.orb, out.getBufferCopy());

        out = new org.jacorb.orb.CDROutputStream(orb);

        //need to write out typecode, to check it's consistency completely
        out.write_TypeCode(in.read_TypeCode());
    }
}
